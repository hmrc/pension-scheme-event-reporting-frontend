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

import connectors.ParsingAndValidationOutcomeCacheConnector
import controllers.actions._
import models.MemberSummaryPath
import models.enumeration.EventType
import models.enumeration.EventType.Event1
import models.fileUpload.ParsingAndValidationOutcome
import models.fileUpload.ParsingAndValidationOutcomeStatus.Success
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fileUpload.FileUploadSuccessView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FileUploadSuccessController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             parsingAndValidationOutcomeCacheConnector: ParsingAndValidationOutcomeCacheConnector,
                                             view: FileUploadSuccessView
                                           )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      val continueUrl = eventType match {
        case Event1 => controllers.event1.routes.UnauthPaymentSummaryController.onPageLoad(waypoints).url
        case _ => controllers.common.routes.MembersSummaryController.onPageLoad(waypoints, MemberSummaryPath(eventType)).url
      }
      parsingAndValidationOutcomeCacheConnector.getOutcome(request.srn).map {
        case Some(ParsingAndValidationOutcome(Success, _, fileName)) =>
          Ok(view(continueUrl, fileName.getOrElse("Your file")))
        case _ => NotFound
      }
  }
}
