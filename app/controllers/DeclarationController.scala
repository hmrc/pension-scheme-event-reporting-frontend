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

import audit.{AuditService, EventReportingReturnEmailAuditEvent}
import config.FrontendAppConfig
import connectors.{EmailConnector, EmailStatus, EventReportingConnector, MinimalConnector}
import controllers.DeclarationController.testDataPsa
import controllers.actions._
import models.enumeration.AdministratorOrPractitioner
import models.requests.DataRequest
import models.{TaxYear, UserAnswers}
import pages.Waypoints
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
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
                                       erConnector: EventReportingConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       emailConnector: EmailConnector,
                                       minimalConnector: MinimalConnector,
                                       auditService: AuditService,
                                       config: FrontendAppConfig,
                                       view: DeclarationView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = identify {
    implicit request =>
      Ok(view(continueUrl = controllers.routes.DeclarationController.onClick(waypoints).url))
  }

  def onClick(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>

      minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).flatMap { minimalDetails =>
        val testUserAnswers: UserAnswers = UserAnswers(testDataPsa(request.pstr))
        val taxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers)
        val email = minimalDetails.email
        val schemeName = request.schemeName

        //TODO: Replace test user answers above with ua when FE captures sufficient data
        erConnector.submitReport(request.pstr, testUserAnswers).flatMap { _ =>
          sendEmail(email, taxYear, schemeName).map {
            _ => Redirect(controllers.routes.ReturnSubmittedController.onPageLoad(waypoints).url)
          }
        }
      }
  }

  private def sendEmail(email: String, taxYear: String, schemeName: String)(
    implicit request: DataRequest[_], hc: HeaderCarrier, messages: Messages): Future[EmailStatus] = {
    val requestId = hc.requestId.map(_.value).getOrElse(request.headers.get("X-Session-ID").getOrElse(""))

    minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).flatMap { minimalDetails =>

      val submittedDate = formatSubmittedDate(ZonedDateTime.now(ZoneId.of("Europe/London")))
      val sendToEmailId = messages("confirmation.whatNext.send.to.email.id")
      val schemeAdministratorType = AdministratorOrPractitioner.Administrator

      val templateParams = Map(
        "psaName" -> minimalDetails.name,
        "schemeName" -> schemeName,
        "taxYear" -> taxYear,
        "dateSubmitted" -> submittedDate,
        "hmrcEmail" -> sendToEmailId //Todo: remove as not sure if needed
      )

      emailConnector.sendEmail(schemeAdministratorType, requestId, request.loggedInUser.idName, email, config.fileReturnTemplateId, templateParams)
        .map { emailStatus =>
          auditService.sendEvent(EventReportingReturnEmailAuditEvent(request.loggedInUser.idName, schemeAdministratorType, email))
          emailStatus
        }
    }
  }
}

object DeclarationController {

  /**
   * The frontend and backend changes for report submission are complete, however the correct data is not captured in the frontend yet
   * Please see the To Do comments below for more info
   * */

  private def testDataPsa(pstr: String): JsObject = {
    Json.obj(
      "declarationDetails" -> Json.obj(
        "erDetails" -> Json.obj(
          "pSTR" -> pstr,
          //TODO: Report start date = tax year start date
          "reportStartDate" -> "2020-04-06",
          //TODO: Report end date = tax year end date
          "reportEndDate" -> "2021-04-05"
        ),
        "erDeclarationDetails" -> Json.obj(
          //TODO: Get PSA ID or PSP ID here from user answers
          "submittedBy" -> "PSA",
          //TODO: Get PSA or PSP ID from user answers
          "submittedID" -> "A2345678"
        ),
        "psaDeclaration" -> Json.obj(
          //TODO: Relates to wantToSubmit and Declaration
          "psaDeclaration1" -> "Selected",
          "psaDeclaration2" -> "Selected"
        )
      )
    )
  }
}