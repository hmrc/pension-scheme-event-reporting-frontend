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

package controllers.fileUpload

import audit.{AuditService, EventReportingUpscanFileUploadAuditEvent}
import config.FrontendAppConfig
import connectors.{EventReportingConnector, UpscanInitiateConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.fileUpload.FileUploadResultFormProvider
import models.FileUploadOutcomeResponse
import models.FileUploadOutcomeStatus.{FAILURE, IN_PROGRESS, SUCCESS}
import models.enumeration.EventType
import models.enumeration.EventType.getEventTypeByName
import models.fileUpload.FileUploadResult
import models.requests.DataRequest
import pages.Waypoints
import pages.fileUpload.FileUploadResultPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fileUpload.{FileUploadResultView, FileUploadView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class FileUploadController @Inject()(val controllerComponents: MessagesControllerComponents,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     eventReportingConnector: EventReportingConnector,
                                     upscanInitiateConnector: UpscanInitiateConnector,
                                     formProvider: FileUploadResultFormProvider,
                                     appConfig: FrontendAppConfig,
                                     view: FileUploadView,
                                     resultView: FileUploadResultView,
                                     auditService: AuditService
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)
    andThen requireData).async { implicit request =>
    val successRedirectUrl = appConfig.successEndPointTarget(eventType)
    val validateRedirectUrl = appConfig.validateEndPointTarget(eventType)
    upscanInitiateConnector.initiateV2(Some(successRedirectUrl), Some(validateRedirectUrl), eventType).map { uir =>

      if (isFileNotFound(request.request.queryString)) {
        Ok(view(waypoints, getEventTypeByName(eventType), eventType, Call("post", uir.postTarget), uir.formFields, noFileUploaded))
      } else {
        Ok(view(waypoints, getEventTypeByName(eventType), eventType, Call("post", uir.postTarget), uir.formFields, getErrorCode(request.queryString)))
      }
    }
  }

  private def getErrorCode(queryString: Map[String, Seq[String]])(implicit messages: Messages): Option[ErrorMessage] = {
    if (queryString.contains("errorCode") && queryString("errorCode").nonEmpty) {
      queryString("errorCode").headOption.map { error =>
        ErrorMessage(content = Text(messages("fileUpload.error.rejected." + error, appConfig.maxUploadFileSize)))
      }
    } else {
      None
    }
  }

  private def isFileNotFound(queryString: Map[String, Seq[String]]) = Try(queryString("errorMessage")) match {
    case Success(value) => value.contains("""'file' field not found""")
    case Failure(_) => false
  }

  private def noFileUploaded(implicit messages: Messages): Option[ErrorMessage] = {
    Some(ErrorMessage(content = HtmlContent(Html(messages("fileUpload.error.rejected.InvalidArgument")))))
  }

  def onSubmit(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) async { implicit request =>
    val successRedirectUrl = appConfig.successEndPointTarget(eventType)
    val validateRedirectUrl = appConfig.validateEndPointTarget(eventType)

    upscanInitiateConnector.initiateV2(Some(successRedirectUrl), Some(validateRedirectUrl), eventType).flatMap{uir =>

      request.queryString.get("key").flatMap(_.headOption) match {
        case Some(uploadIdReference) =>
          val errorCode = getErrorCode(request.queryString)

          if (isFileNotFound(request.request.queryString)) {
            Future.successful(Ok(view(waypoints, getEventTypeByName(eventType), eventType,
              Call("post", uir.postTarget), uir.formFields, noFileUploaded)))
          }
          else if (errorCode.nonEmpty) {
            Future.successful(Ok(view(waypoints, getEventTypeByName(eventType), eventType,
              Call("post", uir.postTarget), uir.formFields, errorCode)))
          }
          else {
            val preparedForm: Form[FileUploadResult] = request.userAnswers.get(FileUploadResultPage(eventType)).fold(form)(form.fill)
            renderView(waypoints, eventType, preparedForm, Ok)
          }
        case _ =>
          Future.successful(BadRequest("Missing key"))
      }
    }
  }

  private def renderView(waypoints: Waypoints, eventType: EventType, preparedForm: Form[FileUploadResult], status: Status)
                        (implicit request: DataRequest[AnyContent]): Future[Result] = {
    val startTime = System.currentTimeMillis
    request.request.queryString.get("key").flatMap(_.headOption) match {
      case Some(uploadIdReference) =>
        val submitUrl = Call("POST", routes.FileUploadResultController.onSubmit(waypoints, eventType).url + s"?key=$uploadIdReference")
        eventReportingConnector.getFileUploadOutcome(uploadIdReference).map {
          case FileUploadOutcomeResponse(_, IN_PROGRESS, _, _, _) =>
            status(resultView(preparedForm, waypoints, getEventTypeByName(eventType), None, submitUrl))
          case fileUploadOutcomeResponse@FileUploadOutcomeResponse(fileName@Some(_), SUCCESS, _, _, _) =>
            sendUpscanFileUploadAuditEvent(eventType, fileUploadOutcomeResponse, startTime)
            status(resultView(preparedForm, waypoints, getEventTypeByName(eventType), fileName, submitUrl))
          case fileUploadOutcomeResponse@FileUploadOutcomeResponse(_, FAILURE, _, _, _) =>
            sendUpscanFileUploadAuditEvent(eventType, fileUploadOutcomeResponse, startTime)
            Redirect(controllers.fileUpload.routes.FileRejectedController.onPageLoad(waypoints, eventType).url)
          case _ => throw new RuntimeException("UploadId reference does not exist")
        }
      case _ => Future.successful(BadRequest("Missing Key"))
    }
  }

  private def sendUpscanFileUploadAuditEvent(
                                              eventType: EventType,
                                              fileUploadOutcomeResponse: FileUploadOutcomeResponse,
                                              startTime: Long)(implicit request: DataRequest[AnyContent]): Unit =
    auditService.sendEvent(
      EventReportingUpscanFileUploadAuditEvent(
        eventType = eventType,
        psaOrPspId = request.loggedInUser.psaIdOrPspId,
        pstr = request.pstr,
        schemeAdministratorType = request.loggedInUser.administratorOrPractitioner,
        outcome = Right(fileUploadOutcomeResponse),
        uploadTimeInMilliSeconds = System.currentTimeMillis - startTime
      )
    )
}
