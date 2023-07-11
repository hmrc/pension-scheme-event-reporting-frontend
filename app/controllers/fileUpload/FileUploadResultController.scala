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

package controllers.fileUpload

import audit.{AuditService, EventReportingUpscanFileDownloadAuditEvent, EventReportingUpscanFileUploadAuditEvent}
import cats.data.Validated.{Invalid, Valid}
import connectors.{EventReportingConnector, ParsingAndValidationOutcomeCacheConnector, UpscanInitiateConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.fileUpload.FileUploadResultFormProvider
import helpers.fileUpload.FileUploadGenericErrorReporter
import models.FileUploadOutcomeStatus.{FAILURE, IN_PROGRESS, SUCCESS}
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event6, getEventTypeByName}
import models.fileUpload.ParsingAndValidationOutcomeStatus._
import models.fileUpload.{FileUploadResult, ParsingAndValidationOutcome}
import models.requests.OptionalDataRequest
import models.{FileUploadOutcomeResponse, UserAnswers}
import pages.Waypoints
import pages.fileUpload.FileUploadResultPage
import play.api.data.Form
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.{JsArray, JsObject, JsPath, Json}
import play.api.mvc._
import services.CompileService
import services.fileUpload.Validator.FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty
import services.fileUpload._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fileUpload.FileUploadResultView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


class FileUploadResultController @Inject()(val controllerComponents: MessagesControllerComponents,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           userAnswersCacheConnector: UserAnswersCacheConnector,
                                           eventReportingConnector: EventReportingConnector,
                                           compileService: CompileService,
                                           upscanInitiateConnector: UpscanInitiateConnector,
                                           formProvider: FileUploadResultFormProvider,
                                           parsingAndValidationOutcomeCacheConnector: ParsingAndValidationOutcomeCacheConnector,
                                           view: FileUploadResultView,
                                           event6Validator: Event6Validator,
                                           event22Validator: Event22Validator,
                                           event23Validator: Event23Validator,
                                           auditService: AuditService
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  private val maximumNumberOfError = 10

  private def renderView(waypoints: Waypoints, eventType: EventType, preparedForm: Form[FileUploadResult], status: Status)
                        (implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    val startTime = System.currentTimeMillis
    request.request.queryString.get("key").flatMap(_.headOption) match {
      case Some(uploadIdReference) =>
        val submitUrl = Call("POST", routes.FileUploadResultController.onSubmit(waypoints, eventType).url + s"?key=$uploadIdReference")
        eventReportingConnector.getFileUploadOutcome(uploadIdReference).map {
          case FileUploadOutcomeResponse(_, IN_PROGRESS, _, _, _) =>
            status(view(preparedForm, waypoints, getEventTypeByName(eventType), None, submitUrl))
          case fileUploadOutcomeResponse@FileUploadOutcomeResponse(fileName@Some(_), SUCCESS, _, _, _) =>
            sendUpscanFileUploadAuditEvent(eventType, fileUploadOutcomeResponse, startTime)
            status(view(preparedForm, waypoints, getEventTypeByName(eventType), fileName, submitUrl))
          case fileUploadOutcomeResponse@FileUploadOutcomeResponse(_, FAILURE, _, _, _) =>
            sendUpscanFileUploadAuditEvent(eventType, fileUploadOutcomeResponse, startTime)
            Redirect(controllers.fileUpload.routes.FileRejectedController.onPageLoad(waypoints, eventType).url)
          case _ => throw new RuntimeException("UploadId reference does not exist")
        }
      case _ => Future.successful(BadRequest("Missing Key"))
    }
  }

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)) async { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(FileUploadResultPage(eventType))).fold(form)(form.fill)
    renderView(waypoints, eventType, preparedForm, Ok)
  }

  def onSubmit(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          renderView(waypoints, eventType, formWithErrors, BadRequest),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(FileUploadResultPage(eventType), value)
          val redirectResultPage: Result = Redirect(FileUploadResultPage(eventType).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            if (value == FileUploadResult.Yes) {
              parsingAndValidationOutcomeCacheConnector
                .deleteOutcome
                .flatMap(_ => asyncGetUpscanFileAndParse(eventType))
              redirectResultPage
            } else {
              redirectResultPage
            }
          }
        }
      )
  }

  private def validatorForEvent(eventType: EventType): Validator = {
    eventType match {
      case Event6 => event6Validator
      case Event22 => event22Validator
      case _ => event23Validator
    }
  }

  private def setGeneralErrorOutcome(errorMessage: String,
                                     fileName: Option[String] = None,
                                     error: Option[Throwable] = None)(implicit request: OptionalDataRequest[AnyContent]): Future[Unit] = {
    error.foreach { e =>
      logger.error(errorMessage, e)
    }
    parsingAndValidationOutcomeCacheConnector.setOutcome(
      ParsingAndValidationOutcome(status = GeneralError, fileName = fileName)
    )
  }

  private def performValidation(eventType: EventType,
                                csvFileContent: String,
                                fileName: Option[String])(implicit request: OptionalDataRequest[AnyContent]): Future[Unit] = {
    val parsedCSV = CSVParser.split(csvFileContent)
    val uaAfterRemovalOfEventType = Try(request.userAnswers
      .getOrElse(UserAnswers()).removeWithPath(JsPath \ s"event${eventType.toString}")) match {
      case scala.util.Success(ua) => ua
      case scala.util.Failure(_) =>
        request.userAnswers.getOrElse(UserAnswers())
    }

    val eventValidator = validatorForEvent(eventType)
    val futureOutcome = eventValidator.validate(parsedCSV, uaAfterRemovalOfEventType) match {
      case Invalid(errors) =>
        Future.successful(processInvalid(eventType, errors))
      case Valid(updatedUA) =>
        userAnswersCacheConnector.save(request.pstr, eventType, updatedUA).flatMap { _ =>
          compileService.compileEvent(eventType, request.pstr, updatedUA)
            .map(_ => ParsingAndValidationOutcome(Success, Json.obj(), fileName))
        }
    }

    futureOutcome.flatMap(outcome => parsingAndValidationOutcomeCacheConnector.setOutcome(outcome))
  }

  private def asyncGetUpscanFileAndParse(eventType: EventType)(implicit request: OptionalDataRequest[AnyContent]): Future[Unit] = {
    val startTime = System.currentTimeMillis
    request.request.queryString.get("key").flatMap(_.headOption) match {
      case Some(reference) =>
        eventReportingConnector.getFileUploadOutcome(reference).flatMap { fileUploadOutcomeResponse =>
          val fileName = fileUploadOutcomeResponse.fileName
          fileUploadOutcomeResponse.downloadUrl match {
            case Some(downloadUrl) =>
              upscanInitiateConnector.download(downloadUrl).flatMap { httpResponse =>
                sendUpscanFileDownloadAuditEvent(eventType, httpResponse.status, startTime, fileUploadOutcomeResponse)
                httpResponse.status match {
                  case OK => performValidation(eventType, httpResponse.body, fileName) recoverWith {
                    case e: Throwable =>
                      setGeneralErrorOutcome(s"Unable to download file: download URL = $downloadUrl", fileName, Some(e))
                  }
                  case e =>
                    setGeneralErrorOutcome(s"Upscan download error response code $e and response body is ${httpResponse.body}", fileName)
                }
              }
            case None => setGeneralErrorOutcome(
              s"No download url: fileuploadstatus = ${fileUploadOutcomeResponse.fileUploadStatus}", fileName)
          }
        }
      case _ => setGeneralErrorOutcome(s"No reference number in FileUploadResultController")
    }
  }

  private def sendUpscanFileUploadAuditEvent(
                                              eventType: EventType,
                                              fileUploadOutcomeResponse: FileUploadOutcomeResponse,
                                              startTime: Long)(implicit request: OptionalDataRequest[AnyContent]): Unit = {

    val endTime = System.currentTimeMillis
    val duration = endTime - startTime

    auditService.sendEvent(
      EventReportingUpscanFileUploadAuditEvent(
        eventType = eventType,
        psaOrPspId = request.loggedInUser.psaIdOrPspId,
        pstr = request.pstr,
        schemeAdministratorType = request.loggedInUser.administratorOrPractitioner,
        outcome = Right(fileUploadOutcomeResponse),
        uploadTimeInMilliSeconds = duration
      )
    )
  }

  private def sendUpscanFileDownloadAuditEvent(eventType: EventType,
                                               responseStatus: Int,
                                               startTime: Long,
                                               fileUploadOutcomeResponse: FileUploadOutcomeResponse)
                                              (implicit request: OptionalDataRequest[AnyContent]): Unit = {

    val endTime = System.currentTimeMillis
    val duration = endTime - startTime
    auditService.sendEvent(
      EventReportingUpscanFileDownloadAuditEvent(
        psaOrPspId = request.loggedInUser.psaIdOrPspId,
        pstr = request.pstr,
        schemeAdministratorType = request.loggedInUser.administratorOrPractitioner,
        eventType = eventType,
        fileUploadOutcomeResponse = fileUploadOutcomeResponse,
        downloadStatus = responseStatus match {
          case 200 => "Success"
          case _ => "Failed"
        },
        downloadTimeInMilliSeconds = duration
      ))
  }

  private def processInvalid(eventType: EventType,
                             errors: Seq[ValidationError])(implicit messages: Messages): ParsingAndValidationOutcome = {
    errors match {
      case Seq(FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty) =>
        ParsingAndValidationOutcome(status = GeneralError)
      case _ =>
        if (errors.size <= maximumNumberOfError) {
          def errorJson(errors: Seq[ValidationError]): JsArray = {
            val cellErrors: Seq[JsObject] = errors.map { e =>
              val cell = String.valueOf(('A' + e.col).toChar) + (e.row + 1)
              Json.obj(
                "cell" -> cell,
                "error" -> messages(e.error, e.args: _*),
                "columnName" -> e.columnName
              )
            }
            Json.arr(cellErrors.map(x => Json.toJsFieldJsValueWrapper(x)): _*)
          }

          ParsingAndValidationOutcome(
            status = ValidationErrorsLessThan10,
            json = Json.obj(
              "errors" -> errorJson(errors)
            )
          )
        } else {
          ParsingAndValidationOutcome(
            status = ValidationErrorsMoreThanOrEqual10,
            json = Json.obj(
              "errors" -> FileUploadGenericErrorReporter.generateGenericErrorReport(errors, eventType),
              "totalErrors" -> errors.size
            )
          )
        }
    }
  }
}
