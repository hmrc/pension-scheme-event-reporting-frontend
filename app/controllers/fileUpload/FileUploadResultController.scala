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

import audit.{AuditService, EventReportingFileValidationAuditEvent, EventReportingUpscanFileDownloadAuditEvent, EventReportingUpscanFileUploadAuditEvent}
import cats.data.Validated
import cats.data.Validated.Invalid
import connectors.{EventReportingConnector, ParsingAndValidationOutcomeCacheConnector, UpscanInitiateConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.fileUpload.FileUploadResultFormProvider
import helpers.fileUpload.FileUploadGenericErrorReporter
import helpers.fileUpload.FileUploadGenericErrorReporter.generateGenericErrorReport
import models.FileUploadOutcomeStatus.{FAILURE, IN_PROGRESS, SUCCESS}
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event24, Event6, getEventTypeByName}
import models.fileUpload.ParsingAndValidationOutcomeStatus._
import models.fileUpload.{FileUploadResult, ParsingAndValidationOutcome}
import models.requests.OptionalDataRequest
import models.{FileUploadOutcomeResponse, TaxYear, UserAnswers}
import org.apache.commons.lang3.StringUtils.EMPTY
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.{Source, StreamConverters}
import org.apache.pekko.util.ByteString
import pages.fileUpload.FileUploadResultPage
import pages.{TaxYearPage, VersionInfoPage, Waypoints}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.{JsObject, JsPath, Json}
import play.api.mvc._
import services.fileUpload.Validator.FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty
import services.fileUpload._
import services.CompileService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FastJsonAccumulator
import views.html.fileUpload.FileUploadResultView

import javax.inject.Inject
import scala.collection.mutable.ArrayBuffer
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
                                           event1Validator: Event1Validator,
                                           event6Validator: Event6Validator,
                                           event22Validator: Event22Validator,
                                           event23Validator: Event23Validator,
                                           event24Validator: Event24Validator,
                                           auditService: AuditService
                                          )(implicit ec: ExecutionContext, as: ActorSystem) extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()

  private val maximumNumberOfError = 10

  private def renderView(waypoints: Waypoints, eventType: EventType, preparedForm: Form[FileUploadResult], status: Status)
                        (implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    val startTime = System.currentTimeMillis
    request.request.queryString.get("key").flatMap(_.headOption) match {
      case Some(uploadIdReference) =>
        val submitUrl = Call("POST", routes.FileUploadResultController.onPageLoad(waypoints, eventType).url + s"?key=$uploadIdReference")
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
                .deleteOutcome(request.srn)
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
      case Event1 => event1Validator
      case Event6 => event6Validator
      case Event22 => event22Validator
      case Event24 => event24Validator
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
      ParsingAndValidationOutcome(status = GeneralError, fileName = fileName), request.srn
    )
  }

  private def performValidation(eventType: EventType,
                                source: Source[ByteString, _],
                                fileName: Option[String])(implicit request: OptionalDataRequest[AnyContent]): Future[Unit] = {
    val startTime = System.currentTimeMillis

    val inputStream = source.runWith(StreamConverters.asInputStream())

    val uaAfterRemovalOfEventType = Try(request.userAnswers
      .getOrElse(UserAnswers()).removeWithPath(JsPath \ s"event${eventType.toString}")) match {
      case scala.util.Success(ua) => ua
      case scala.util.Failure(_) =>
        request.userAnswers.getOrElse(UserAnswers())
    }


    val validator = validatorForEvent(eventType)


    val taxYear = TaxYear.getTaxYear(uaAfterRemovalOfEventType)

    val parserResultFuture = CSVParser.split(inputStream)(new FastJsonAccumulator() -> new ArrayBuffer[ValidationError]()) {
      case ((dataAccumulator, errorAccumulator), row, rowNumber) =>
        validator.validate(rowNumber, row.toIndexedSeq, dataAccumulator, errorAccumulator, taxYear)
    }

    parserResultFuture.flatMap { case ((dataAccumulator, errorAccumulator), rowNumber) =>
      inputStream.close()
      if(rowNumber < 2) {
        errorAccumulator += FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty
      }

      val compileEventResponse = errorAccumulator match {
        case ArrayBuffer() =>
          (uaAfterRemovalOfEventType.get(TaxYearPage), uaAfterRemovalOfEventType.get(VersionInfoPage)) match {
            case (Some(year), Some(version)) =>
              userAnswersCacheConnector.save(request.pstr, eventType, dataAccumulator.toJson, year.startYear, version.version.toString, request.srn).flatMap { _ =>
                compileService.compileEvent(eventType, request.pstr, uaAfterRemovalOfEventType)
                  .map(_ => ParsingAndValidationOutcome(Success, Json.obj(), fileName))
              }
            case (y, v) =>
              Future.failed(new RuntimeException(s"No tax year or version available: $y / $v"))
          }
        case errors =>
          Future.successful(processInvalid(eventType, errors))
      }

      compileEventResponse.flatMap { outcome =>
        val endTime = System.currentTimeMillis
        sendValidationAuditEvent(pstr = request.pstr, eventType = eventType, numberOfEntries = rowNumber - 1, fileValidationTimeInSeconds = (endTime - startTime) / 1000, parserResult = Invalid(errorAccumulator.toSeq))
        parsingAndValidationOutcomeCacheConnector.setOutcome(outcome, request.srn)
      }
    }
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
                  case OK => performValidation(eventType, httpResponse.bodyAsSource, fileName) recoverWith {
                    case e: Throwable =>
                      setGeneralErrorOutcome("Unable to download file", fileName, Some(e))
                  }
                  case e =>
                    setGeneralErrorOutcome(s"Upscan download error response code $e", fileName)
                }
              }
            case None => setGeneralErrorOutcome(
              s"No download url: fileuploadstatus = ${fileUploadOutcomeResponse.fileUploadStatus}", fileName)
          }
        }
      case _ => setGeneralErrorOutcome("No reference number in FileUploadResultController")
    }
  }

  private def failureReasonAndErrorReportForAudit(errors: Seq[ValidationError],
                                                  eventType: EventType)(implicit messages: Messages): Option[(String, String)] = {
    if (errors.isEmpty) {
      None
    } else if (errors.size <= maximumNumberOfError) {
      val errorReport = errorJson(errors, messages).foldLeft("") { (acc, jsObject) =>
        ((jsObject \ "cell").asOpt[String], (jsObject \ "error").asOpt[String]) match {
          case (Some(cell), Some(error)) => acc ++ ((if (acc.nonEmpty) "\n" else EMPTY) + s"$cell: $error")
          case _ => acc
        }
      }
      Some(Tuple2("Field Validation failure(Less than 10)", errorReport))
    } else {
      val errorReport = generateGenericErrorReport(errors, eventType).foldLeft(EMPTY) { (acc, c) =>
        acc ++ (if (acc.nonEmpty) "\n" else EMPTY) + messages(c)
      }
      Some(Tuple2("Generic failure (more than 10)", errorReport))
    }
  }

  private def sendValidationAuditEvent(pstr: String,
                                       eventType: EventType,
                                       numberOfEntries: Int,
                                       fileValidationTimeInSeconds: Long,
                                       parserResult: Validated[Seq[ValidationError], UserAnswers]
                                      )(implicit request: OptionalDataRequest[AnyContent], messages: Messages): Unit = {

    val numberOfFailures = parserResult.fold(_.size, _ => 0)
    val (failureReason, errorReport) = parserResult match {
      case Invalid(Seq(FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty)) =>
        Tuple2(Some(FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty.error), None)
      case Invalid(errors) =>
        failureReasonAndErrorReportForAudit(errors, eventType)(messages) match {
          case Some(Tuple2(reason, report)) => Tuple2(Some(reason), Some(report))
          case _ => Tuple2(None, None)
        }
      case _ => Tuple2(None, None)
    }

    auditService.sendEvent(
      EventReportingFileValidationAuditEvent(
        schemeAdministratorType = request.loggedInUser.administratorOrPractitioner,
        psaOrPspId = request.loggedInUser.psaIdOrPspId,
        pstr = pstr,
        numberOfEntries = numberOfEntries,
        eventType = eventType,
        validationCheckSuccessful = parserResult.isValid,
        fileValidationTimeInSeconds = fileValidationTimeInSeconds,
        failureReason = failureReason,
        numberOfFailures = numberOfFailures,
        validationFailureContent = errorReport
      )
    )
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
                             errors: ArrayBuffer[ValidationError])(implicit messages: Messages): ParsingAndValidationOutcome = {
    errors match {
      case Seq(FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty) =>
        ParsingAndValidationOutcome(status = IncorrectHeadersOrEmptyFile)
      case _ =>
        if (errors.size <= maximumNumberOfError) {
          ParsingAndValidationOutcome(
            status = ValidationErrorsLessThan10,
            json = Json.obj(
              "errors" -> Json.arr(errorJson(errors.toSeq, messages).map(x => Json.toJsFieldJsValueWrapper(x)): _*)
            )
          )
        } else {
          ParsingAndValidationOutcome(
            status = ValidationErrorsMoreThanOrEqual10,
            json = Json.obj(
              "errors" -> FileUploadGenericErrorReporter.generateGenericErrorReport(errors.toSeq, eventType),
              "totalErrors" -> errors.size
            )
          )
        }
    }
  }

  def errorJson(errors: Seq[ValidationError], messages: Messages): Seq[JsObject] = {
    val cellErrors: Seq[JsObject] = errors.map { e =>
      val cell = String.valueOf(('A' + e.col).toChar) + (e.row + 1)
      Json.obj(
        "cell" -> cell,
        "error" -> messages(e.error, e.args: _*),
        "columnName" -> e.columnName
      )
    }
    cellErrors
  }
}
