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

import connectors.ParsingAndValidationOutcomeCacheConnector
import controllers.actions._
import models.enumeration.EventType
import models.fileUpload.ParsingAndValidationOutcome
import models.fileUpload.ParsingAndValidationOutcomeStatus.{GeneralError, Success, ValidationErrorsLessThan10, ValidationErrorsMoreThanOrEqual10}
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fileUpload.ProcessingRequestView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ProcessingRequestController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: ProcessingRequestView,
                                             parsingAndValidationOutcomeCacheConnector: ParsingAndValidationOutcomeCacheConnector
                                           )(implicit ec: ExecutionContext)

  extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = identify.async {
    implicit request =>
      parsingAndValidationOutcomeCacheConnector.getOutcome.flatMap {
        case Some(ParsingAndValidationOutcome(Success, _, _)) =>
          Future.successful(Redirect(controllers.fileUpload.routes.FileUploadSuccessController.onPageLoad(waypoints, eventType)))
        case Some(ParsingAndValidationOutcome(GeneralError, _, _)) =>
          Future.successful(Redirect(controllers.fileUpload.routes.ProblemWithServiceController.onPageLoad(waypoints, eventType)))
        case Some(ParsingAndValidationOutcome(ValidationErrorsLessThan10, _, _)) =>
          Future.successful(Redirect(controllers.fileUpload.routes.ValidationErrorsAllController.onPageLoad(waypoints, eventType)))
        case Some(ParsingAndValidationOutcome(ValidationErrorsMoreThanOrEqual10, _, _)) =>
          Future.successful(Redirect(controllers.fileUpload.routes.ValidationErrorsSummaryController.onPageLoad(waypoints, eventType)))
        case Some(outcome) => throw new RuntimeException(s"Unknown outcome $outcome")
        case _ =>
          Future.successful(Ok(view(controllers.routes.JourneyRecoveryController.onPageLoad(None).url)))
      }
  }

  def onPageLoadStatus(): Action[AnyContent] = identify.async {
    implicit request =>
      val outcomes = parsingAndValidationOutcomeCacheConnector.getOutcome.flatMap {
        case Some(ParsingAndValidationOutcome(Success, _, _)) =>
          Future.successful(Json.obj("status" -> "processed"))
        case Some(ParsingAndValidationOutcome(GeneralError, _, _)) =>
          Future.successful(Json.obj("status" -> "processed"))
        case Some(ParsingAndValidationOutcome(ValidationErrorsLessThan10, _, _)) =>
          Future.successful(Json.obj("status" -> "processed"))
        case Some(ParsingAndValidationOutcome(ValidationErrorsMoreThanOrEqual10, _, _)) =>
          Future.successful(Json.obj("status" -> "processed"))
        case Some(outcome) => throw new RuntimeException(s"Unknown outcome $outcome")
        case _ =>
          Future.successful(Json.obj("status" -> "processing"))
      }
      outcomes.map { outcome =>
        Results.Ok(outcome.toString)
      }
  }
}
