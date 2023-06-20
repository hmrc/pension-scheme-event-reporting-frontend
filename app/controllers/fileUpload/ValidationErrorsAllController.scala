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
import models.fileUpload.ParsingAndValidationOutcomeStatus.ValidationErrorsLessThan10
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Json, JsonValidationError}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ValidationErrorForRendering
import views.html.fileUpload.ValidationErrorsAllView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ValidationErrorsAllController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: ValidationErrorsAllView,
                                               parsingAndValidationOutcomeCacheConnector: ParsingAndValidationOutcomeCacheConnector
                                             )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event22

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      val returnUrl = controllers.fileUpload.routes.FileUploadController.onPageLoad(waypoints).url
      val fileDownloadInstructionLink = controllers.routes.FileDownloadController.instructionsFile.url
      parsingAndValidationOutcomeCacheConnector.getOutcome.map {
        case Some(outcome@ParsingAndValidationOutcome(ValidationErrorsLessThan10, _, _)) =>
           outcome.json.validate[Seq[ValidationErrorForRendering]] match {
            case JsSuccess(value, _) => Ok(view(returnUrl, fileDownloadInstructionLink, value))
            case JsError(errors) => throw JsResultException(errors)
          }
        case _ => NotFound
      }
  }
}
