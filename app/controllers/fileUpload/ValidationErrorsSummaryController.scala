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
import models.fileUpload.ParsingAndValidationOutcomeStatus.ValidationErrorsMoreThanOrEqual10
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fileUpload.ValidationErrorsSummaryView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ValidationErrorsSummaryController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   parsingAndValidationOutcomeCacheConnector: ParsingAndValidationOutcomeCacheConnector,
                                                   view: ValidationErrorsSummaryView
                                                 )(implicit Ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event22

  private def generateAllErrors(parsingAndValidationOutcome: ParsingAndValidationOutcome): Seq[String] = Nil

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      val returnUrl = controllers.fileUpload.routes.FileUploadController.onPageLoad(waypoints).url
      val fileDownloadInstructionLink = controllers.routes.FileDownloadController.instructionsFile.url

      parsingAndValidationOutcomeCacheConnector.getOutcome.map {
        case Some(outcome@ParsingAndValidationOutcome(ValidationErrorsMoreThanOrEqual10, _, _)) =>
          Ok(view(returnUrl, fileDownloadInstructionLink, generateAllErrors(outcome)))
        case _ => NotFound
      }
  }
}
