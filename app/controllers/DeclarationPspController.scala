/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
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
                                         erConnector: EventReportingConnector,
                                         schemeDetailsConnector: SchemeDetailsConnector,
                                         emailConnector: EmailConnector,
                                         minimalConnector: MinimalConnector,
                                         auditService: AuditService,
                                         config: FrontendAppConfig,
                                         view: DeclarationPspView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(authorisingPsaId: Option[String]): Form[String] = formProvider(authorisingPSAID = authorisingPsaId)


  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()).async { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(DeclarationPspPage)) match {
      case Some(value) => form(authorisingPsaId = None).fill(value)
      case None => form(authorisingPsaId = None)
    }
    minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).map {
      minimalDetails =>
        Ok(view(minimalDetails.name, preparedForm, waypoints))
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>

      def emailFuture = minimalConnector.getMinimalDetails(
        request.loggedInUser.idName,
        request.loggedInUser.psaIdOrPspId).flatMap { minimalDetails =>
        val taxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers)
        val email = minimalDetails.email
        val schemeName = request.schemeName
        sendEmail(minimalDetails.name, email, taxYear, schemeName)
      }

      schemeDetailsConnector.getPspSchemeDetails(request.loggedInUser.psaIdOrPspId, request.pstr).map(_.authorisingPSAID).flatMap { authorisingPsaId =>
        minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).flatMap { minimalDetails =>
          form(authorisingPsaId = authorisingPsaId)
            .bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(minimalDetails.name, formWithErrors, waypoints))),
            value => {
              val originalUserAnswers = request.userAnswers
              val reportVersion = originalUserAnswers.get(VersionInfoPage).get.version.toString
              val updatedAnswers = originalUserAnswers.setOrException(DeclarationPspPage, value)
              userAnswersCacheConnector.save(request.pstr, updatedAnswers).flatMap { _ =>
                declarationData(request.pstr, TaxYear.getSelectedTaxYear(request.userAnswers), request.loggedInUser, authorisingPsaId) match {
                  case Some(data) =>
                    erConnector.submitReport(request.pstr, UserAnswers(data), reportVersion).flatMap { _ =>
                      emailFuture.map { _ =>
                        Redirect(controllers.routes.ReturnSubmittedController.onPageLoad(waypoints))
                      }
                    }
                  case _ => Future.successful(Redirect(controllers.routes.IndexController.onPageLoad.url))
                }
              }
            }
          )
        }
      }
  }

  private def sendEmail(pspName: String, email: String, taxYear: String, schemeName: String)(
    implicit request: DataRequest[_], hc: HeaderCarrier): Future[EmailStatus] = {
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

  private def declarationData(pstr: String, taxYear: TaxYear, loggedInUser: LoggedInUser, optAuthorisingPsaId: Option[String]) = {
    val psaOrPsp = loggedInUser.administratorOrPractitioner match {
      case AdministratorOrPractitioner.Administrator => "PSA"
      case AdministratorOrPractitioner.Practitioner => "PSP"
      case _ => throw new RuntimeException("Unknown user type")
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
