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
import connectors.{EmailConnector, EmailStatus, MinimalConnector}
import controllers.actions._
import handlers.NothingToSubmitException
import models.enumeration.AdministratorOrPractitioner
import models.requests.DataRequest
import models.{LoggedInUser, TaxYear, UserAnswers}
import pages.{VersionInfoPage, Waypoints}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmitService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateHelper.formatSubmittedDate
import views.html.DeclarationView

import java.time.{ZoneId, ZonedDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       submitService: SubmitService,
                                       val controllerComponents: MessagesControllerComponents,
                                       emailConnector: EmailConnector,
                                       minimalConnector: MinimalConnector,
                                       auditService: AuditService,
                                       config: FrontendAppConfig,
                                       declarationView: DeclarationView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  private val logger = Logger(classOf[DeclarationController])

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData) {
    implicit request =>
      if (request.isReportSubmitted) {
        Redirect(controllers.routes.CannotResumeController.onPageLoad(waypoints))
      } else {
        Ok(declarationView(continueUrl = controllers.routes.DeclarationController.onClick(waypoints).url))
      }
  }

  def onClick(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()).async {
    implicit request =>

      request.userAnswers.getOrElse(throw new NothingToSubmitException("User data not available"))

      requireData.invokeBlock(request, { implicit request: DataRequest[_] =>
        val data: UserAnswers = UserAnswers(
          declarationData(
            request.pstr,
            TaxYear.getSelectedTaxYear(request.userAnswers),
            request.loggedInUser),
          request.userAnswers.noEventTypeData
        )

        def emailFuture: Future[EmailStatus] = minimalConnector.getMinimalDetails(
          request.loggedInUser.idName,
          request.loggedInUser.psaIdOrPspId).flatMap { minimalDetails =>
          val taxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers)
          val email = minimalDetails.email
          val schemeName = request.schemeName
          sendEmail(minimalDetails.name, email, taxYear, schemeName)
        }

        submitService.submitReport(request.pstr, data).flatMap { result =>
          result.header.status match {
            case OK => emailFuture.map(_ => Redirect(controllers.routes.ReturnSubmittedController.onPageLoad(waypoints).url))
            case BAD_REQUEST =>
              logger.warn(s"Unable to submit declaration because it has already been submitted)")
              Future.successful(Redirect(controllers.routes.CannotResumeController.onPageLoad(waypoints).url))
            case NOT_FOUND =>
              logger.warn(s"Unable to submit declaration because there is nothing to submit (nothing in compile state)")
              Future.successful(Redirect(controllers.routes.EventSummaryController.onPageLoad(waypoints).url))
            case _ => throw new RuntimeException(s"Invalid response returned from submit report: ${result.header.status}")
          }
        }
      })
  }

  private def sendEmail(psaName: String, email: String, taxYear: String, schemeName: String)(
    implicit request: DataRequest[_], hc: HeaderCarrier): Future[EmailStatus] = {
    val requestId = hc.requestId.map(_.value).getOrElse(request.headers.get("X-Session-ID").getOrElse(""))

    val submittedDate = formatSubmittedDate(ZonedDateTime.now(ZoneId.of("Europe/London")))
    val schemeAdministratorType = AdministratorOrPractitioner.Administrator

    val templateParams = Map(
      "psaName" -> psaName,
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

  private def declarationData(pstr: String, taxYear: TaxYear, loggedInUser: LoggedInUser): JsObject = {

    val psaOrPsp = loggedInUser.administratorOrPractitioner match {
      case AdministratorOrPractitioner.Administrator => "PSA"
      case AdministratorOrPractitioner.Practitioner => "PSP"
      case _ => throw new RuntimeException("Unknown user type")
    }

    Json.obj(
      "pstr" -> pstr,
      "reportStartDate" -> s"${taxYear.startYear}-04-06",
      "reportEndDate" -> s"${taxYear.endYear}-04-05",
      "submittedBy" -> psaOrPsp,
      "submittedID" -> loggedInUser.psaIdOrPspId,
      "psaDeclaration1" -> "Selected",
      "psaDeclaration2" -> "Selected"
    )
  }
}
