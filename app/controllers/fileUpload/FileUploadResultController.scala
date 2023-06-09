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

import connectors.{EventReportingConnector, ParsingAndValidationOutcomeCacheConnector, UpscanInitiateConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import fileUploadParser.CSVParser
import forms.fileUpload.FileUploadResultFormProvider
import models.FileUploadOutcomeStatus.IN_PROGRESS
import models.FileUploadOutcomeStatus.SUCCESS
import models.FileUploadOutcomeStatus.FAILURE
import models.FileUploadOutcomeResponse
import models.UserAnswers
import models.enumeration.EventType
import models.enumeration.EventType.getEventTypeByName
import models.fileUpload.ParsingAndValidationOutcomeStatus._
import models.fileUpload.{FileUploadResult, ParsingAndValidationOutcome}
import models.requests.OptionalDataRequest
import pages.Waypoints
import pages.fileUpload.FileUploadResultPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.i18n.Lang.logger
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Request}
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
                                           view: FileUploadResultView
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

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
            val parsingResult = if (value == FileUploadResult.Yes) {
              parsingAndValidationOutcomeCacheConnector.deleteOutcome.map { _ =>
                getUpscanFileAndParse.flatten
              }
            } else {
              Future.successful(())
            }
            parsingResult.map(_ => Redirect(FileUploadResultPage(eventType).navigate(waypoints, originalUserAnswers, updatedAnswers).route))
          }
        }
      )
  }

  private def getUpscanFileAndParse(implicit request: Request[AnyContent]) = {
    request.queryString.get("key")
    val referenceOpt: Option[String] = request.queryString.get("key").flatMap { values =>
      values.headOption
    }

    referenceOpt match {
      case Some(reference) =>
        eventReportingConnector.getFileUploadOutcome(reference).flatMap { fileUploadOutcomeResponse =>
          val fileName = fileUploadOutcomeResponse.fileName
          fileUploadOutcomeResponse.downloadUrl match {
            case Some(downloadUrl) =>
              upscanInitiateConnector.download(downloadUrl).map { httpResponse =>
                httpResponse.status match {
                  case OK => {
                    CSVParser.split(httpResponse.body).foreach { row =>
                      //TODO - This for loop is temporary code to allow parsing to be printed in the console for testing
                      //TODO - To be removed at a later stage
                      val formattedRow: String = row.mkString(",")
                      println(s"\n Formatted row: $formattedRow")
                    }
                    parsingAndValidationOutcomeCacheConnector.setOutcome(outcome = ParsingAndValidationOutcome(status = Success, fileName = fileName))
                    Future.successful(ParsingAndValidationOutcome(Success))
                  } recoverWith {
                    case e: Throwable =>
                      logger.error("Error during parsing and validation", e)
                      parsingAndValidationOutcomeCacheConnector.setOutcome(outcome = ParsingAndValidationOutcome(status = GeneralError, fileName = fileName))
                      Future.successful(ParsingAndValidationOutcome(GeneralError))
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
}

