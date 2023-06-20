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
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.fileUpload.ValidationError
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fileUpload.ValidationErrorsAllView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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

//      val dummyErrors: Seq[ValidationError] = Seq(
//        ValidationError(6, 1, "Enter the member's first name", "Column name"),
//        ValidationError(5, 2, "Enter a National Insurance number that is 2 letters, 6 numbers, then A, B, C, or D, like QQ123456C", "Column name"),
//        ValidationError(4, 3, "The charge amount must be an amount of money, like 123 or 123.45", "Column name"),
//        ValidationError(3, 4, "Enter the date you received the notice to pay the charge", "Column name"),
//        ValidationError(2, 5, "Select yes if the payment type is mandatory", "Column name"),
//        ValidationError(1, 6, "Enter the tax year to which the annual allowance charge relates", "Column name")
//      )
//      Future.successful(Ok(view(returnUrl, fileDownloadInstructionLink, dummyErrors)))

          parsingAndValidationOutcomeCacheConnector.getOutcome.map { x =>

            println("\n<>>>" + x)

            x match {
              case Some(ParsingAndValidationOutcome(ValidationErrorsLessThan10, _, errorsJson, _, _)) =>

                Ok(view(returnUrl, fileDownloadInstructionLink, errorsJson))
              case _ =>
                NotFound
            }
          }
  }
}
