/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import audit.{AuditService, EventReportingSubmissionEmailAuditEvent}
import config.FrontendAppConfig
import connectors._
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.DeclarationPspFormProvider
import models.enumeration.AdministratorOrPractitioner
import models.requests.DataRequest
import models.{LoggedInUser, TaxYear, UserAnswers}
import pages.{DeclarationPspPage, VersionInfoPage, Waypoints}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmitService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateHelper.formatSubmittedDate
import views.html.DeclarationPspView

import java.time.{ZoneId, ZonedDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationPspController @Inject()(val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         formProvider: DeclarationPspFormProvider,
                                         requireData: DataRequiredAction,
                                         submitService: SubmitService,
                                         schemeDetailsConnector: SchemeConnector,
                                         emailConnector: EmailConnector,
                                         minimalConnector: MinimalConnector,
                                         auditService: AuditService,
                                         config: FrontendAppConfig,
                                         view: DeclarationPspView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  private val logger = Logger(classOf[DeclarationController])

  private def form(authorisingPsaId: Option[String]): Form[String] = formProvider(authorisingPSAID = authorisingPsaId)

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()).async { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(DeclarationPspPage)) match {
      case Some(value) => form(authorisingPsaId = None).fill(value)
      case None => form(authorisingPsaId = None)
    }
    minimalConnector.getMinimalDetails(request.loggedInUser.idName).map {
      minimalDetails =>
        if (request.isReportSubmitted) {
          Redirect(controllers.routes.CannotResumeController.onPageLoad(waypoints))
        } else {
          Ok(view(minimalDetails.name, preparedForm, waypoints))
        }
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>

        def emailFuture: Future[EmailStatus] = minimalConnector.getMinimalDetails(
          request.loggedInUser.idName).flatMap { minimalDetails =>
          val taxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers)
          val email = minimalDetails.email
          val schemeName = request.schemeName
          sendEmail(minimalDetails.name, email, taxYear, schemeName)
        }

        schemeDetailsConnector.getPspSchemeDetails(request.loggedInUser.psaIdOrPspId, request.pstr, request.srn).map(_.pspDetails.map {_.authorisingPSAID}).flatMap { authorisingPsaId =>
          minimalConnector.getMinimalDetails(request.loggedInUser.idName).flatMap { minimalDetails =>
            form(authorisingPsaId = authorisingPsaId)
              .bindFromRequest().fold(
              formWithErrors =>
                Future.successful(BadRequest(view(minimalDetails.name, formWithErrors, waypoints))),
              value => {
                val originalUserAnswers = request.userAnswers
                val updatedAnswers = originalUserAnswers.setOrException(DeclarationPspPage, value)
                userAnswersCacheConnector.save(request.pstr, updatedAnswers).flatMap { _ =>
                  declarationData(request.pstr, TaxYear.getSelectedTaxYear(request.userAnswers), request.loggedInUser, authorisingPsaId) match {
                    case Some(data) =>
                      val uaWithData = UserAnswers(data, request.userAnswers.noEventTypeData)
                      submitService.submitReport(request.pstr, uaWithData).flatMap { result =>
                        result.header.status match {
                          case OK =>
                            emailFuture.map(_ => Redirect(controllers.routes.ReturnSubmittedController.onPageLoad(waypoints).url))
                          case BAD_REQUEST =>
                            logger.warn(s"Unable to submit declaration because it has already been submitted)")
                            Future.successful(Redirect(controllers.routes.CannotResumeController.onPageLoad(waypoints).url))
                          case NOT_FOUND =>
                            logger.warn(s"Unable to submit declaration because there is nothing to submit (nothing in compile state)")
                            Future.successful(Redirect(controllers.routes.EventSummaryController.onPageLoad(waypoints).url))
                          case _ => throw new RuntimeException(s"Invalid response returned from submit report: ${result.header.status}")
                        }
                      }
                    case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad(None).url))
                  }
                }
              }
            )
          }
        }
  }

  private def sendEmail(pspName: String, email: String, taxYear: String, schemeName: String)(
    implicit request: DataRequest[?], hc: HeaderCarrier): Future[EmailStatus] = {
    val requestId = hc.requestId.map(_.value).getOrElse(request.headers.get("X-Session-ID").getOrElse(""))

    val submittedDate = formatSubmittedDate(ZonedDateTime.now(ZoneId.of("Europe/London")))
    val schemeAdministratorType = AdministratorOrPractitioner.Practitioner

    val templateParams = Map(
      "psaName" -> pspName,
      "schemeName" -> schemeName,
      "taxYear" -> taxYear,
      "dateSubmitted" -> submittedDate
    )

    val reportVersion = request.userAnswers.get(VersionInfoPage).get.version.toString

    emailConnector.sendEmail(schemeAdministratorType,
      requestId,
      request.loggedInUser.psaIdOrPspId,
      request.pstr,
      email,
      config.fileReturnTemplateId,
      templateParams,
      reportVersion).map { emailStatus =>
      auditService.sendEvent(
        EventReportingSubmissionEmailAuditEvent(
          request.loggedInUser.psaIdOrPspId,
          request.pstr,
          schemeAdministratorType,
          email,
          reportVersion,
          emailStatus
        )
      )
      emailStatus
    }
  }

  private def declarationData(pstr: String, taxYear: TaxYear, loggedInUser: LoggedInUser, optAuthorisingPsaId: Option[String]): Option[JsObject] = {
    val psaOrPsp = loggedInUser.administratorOrPractitioner match {
      case AdministratorOrPractitioner.Administrator => "PSA"
      case AdministratorOrPractitioner.Practitioner => "PSP"
    }
    optAuthorisingPsaId.map { authorisingPsaId =>
      Json.obj(
        "pstr" -> pstr,
        "reportStartDate" -> s"${taxYear.startYear}-04-06",
        "reportEndDate" -> s"${taxYear.endYear}-04-05",
        "submittedBy" -> psaOrPsp,
        "submittedID" -> loggedInUser.psaIdOrPspId,
        "authorisedPSAID" -> authorisingPsaId,
        "pspDeclaration1" -> "Selected",
        "pspDeclaration2" -> "Selected"
      )
    }
  }
}
