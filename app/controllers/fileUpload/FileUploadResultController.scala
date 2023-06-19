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

import cats.data.Validated.{Invalid, Valid}
import connectors.{EventReportingConnector, ParsingAndValidationOutcomeCacheConnector, UpscanInitiateConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.fileUpload.FileUploadResultFormProvider
import helpers.fileUpload.FileUploadGenericErrorReporter
import models.FileUploadOutcomeStatus.{FAILURE, IN_PROGRESS, SUCCESS}
import models.enumeration.EventType
import models.enumeration.EventType.getEventTypeByName
import models.fileUpload.ParsingAndValidationOutcomeStatus._
import models.fileUpload.{FileUploadResult, ParsingAndValidationOutcome}
import models.requests.OptionalDataRequest
import models.{FileUploadOutcomeResponse, UserAnswers}
import pages.Waypoints
import pages.fileUpload.FileUploadResultPage
import play.api.data.Form
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.fileUpload.Validator.FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty
import services.fileUpload.{CSVParser, Event22Validator, ValidationError}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fileUpload.FileUploadResultView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileUploadResultController @Inject()(val controllerComponents: MessagesControllerComponents,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           userAnswersCacheConnector: UserAnswersCacheConnector,
                                           eventReportingConnector: EventReportingConnector,
                                           upscanInitiateConnector: UpscanInitiateConnector,
                                           formProvider: FileUploadResultFormProvider,
                                           parsingAndValidationOutcomeCacheConnector: ParsingAndValidationOutcomeCacheConnector,
                                           view: FileUploadResultView,
                                           event22Validator: Event22Validator
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  private val maximumNumberOfError = 10

  private def renderView(waypoints: Waypoints, eventType: EventType, preparedForm: Form[FileUploadResult], status: Status)
                        (implicit request: OptionalDataRequest[AnyContent]) = {
    request.request.queryString.get("key").flatMap(_.headOption) match {
      case Some(uploadIdReference) =>
        val submitUrl = Call("POST", routes.FileUploadResultController.onSubmit(waypoints).url + s"?key=$uploadIdReference")
        eventReportingConnector.getFileUploadOutcome(uploadIdReference).map {
          case FileUploadOutcomeResponse(_, IN_PROGRESS, _) =>
            status(view(preparedForm, waypoints, getEventTypeByName(eventType), None, submitUrl))
          case FileUploadOutcomeResponse(fileName@Some(_), SUCCESS, _) =>
            status(view(preparedForm, waypoints, getEventTypeByName(eventType), fileName, submitUrl))
          case FileUploadOutcomeResponse(_, FAILURE, _) =>
            Redirect(controllers.fileUpload.routes.FileRejectedController.onPageLoad(waypoints).url)
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
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).flatMap { _ =>
            if (value == FileUploadResult.Yes) {
              parsingAndValidationOutcomeCacheConnector.deleteOutcome.map { _ =>
                asyncGetUpscanFileAndParse(eventType)
              }
            } else {
              Future.successful(())
            }
            Future.successful(Redirect(FileUploadResultPage(eventType).navigate(waypoints, originalUserAnswers, updatedAnswers).route))
          }
        }
      )
  }

  private def asyncGetUpscanFileAndParse(eventType: EventType)(implicit request: OptionalDataRequest[AnyContent]): Unit = {
    request.queryString.get("key")
    val referenceOpt: Option[String] = request.request.queryString.get("key").flatMap { values =>
      values.headOption
    }

    referenceOpt match {
      case Some(reference) =>
        eventReportingConnector.getFileUploadOutcome(reference).flatMap { fileUploadOutcomeResponse =>
          val fileName = fileUploadOutcomeResponse.fileName
          fileUploadOutcomeResponse.downloadUrl match {
            case Some(downloadUrl) =>
              upscanInitiateConnector.download(downloadUrl).flatMap { httpResponse =>
                httpResponse.status match {
                  case OK => {
                    val parsedCSV = CSVParser.split(httpResponse.body)
                    val futureOutcome = event22Validator.parse(parsedCSV, request.userAnswers.getOrElse(UserAnswers())) match {
                      case Invalid(errors) => Future.successful(processInvalid(eventType, errors))
                      case Valid(updatedUA) =>
                        eventReportingConnector.compileEvent(request.pstr, updatedUA.eventDataIdentifier(eventType))
                          .map(_ => ParsingAndValidationOutcome(Success))
                    }
                    futureOutcome.map { outcome =>
                      parsingAndValidationOutcomeCacheConnector.setOutcome(outcome)
                    }
                  } recoverWith {
                    case e: Throwable =>
                      logger.error("Error during parsing and validation", e)
                      parsingAndValidationOutcomeCacheConnector.setOutcome(outcome = ParsingAndValidationOutcome(status = GeneralError, fileName = fileName))
                  }
                  case _ => throw new RuntimeException("Unhandled response from upscan in FileUploadResultController")
                }
              }
            case None => throw new RuntimeException("No download url in FileUploadResultController")
          }
        }
      case _ => throw new RuntimeException("No reference number in FileUploadResultController")
    }
  }


  private def processInvalid(eventType: EventType,
                             errors: Seq[ValidationError])(implicit messages: Messages): ParsingAndValidationOutcome = {
    errors match {
      case Seq(FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty) =>
        ParsingAndValidationOutcome(status = GeneralError)
      case _ =>
        if (errors.size <= maximumNumberOfError) {
          val cellErrors: Seq[JsObject] = errorJson(errors, messages)
          ParsingAndValidationOutcome(
            status = ValidationErrorsLessThan10,
            json = Json.obj("errors" -> cellErrors)
          )
        } else {
          ParsingAndValidationOutcome(
            status = ValidationErrorsMoreThanOrEqual10,
            json = Json.obj(
              "errors" -> FileUploadGenericErrorReporter.generateGenericErrorReport(errors, eventType),
              "totalError" -> errors.size
            )
          )
        }
    }
  }

  private def errorJson(errors: Seq[ValidationError], messages: Messages): Seq[JsObject] = {
    val cellErrors = errors.map { e =>
      val cell = String.valueOf(('A' + e.col).toChar) + (e.row + 1)
      Json.obj(
        "cell" -> cell,
        "error" -> messages(e.error, e.args: _*)
      )
    }
    cellErrors
  }

}

