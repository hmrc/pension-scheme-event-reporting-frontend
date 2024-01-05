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

package controllers.common

import controllers.actions._
import models.enumeration.EventType
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.common.FileUploadWhatYouWillNeedView

import javax.inject.Inject

class FileUploadWhatYouWillNeedController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: FileUploadWhatYouWillNeedView
                                                   ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)) {
    implicit request =>
      val templateDownloadLink = controllers.routes.FileDownloadController.templateFile(eventType)
      val instructionsDownloadLink = controllers.routes.FileDownloadController.instructionsFile(eventType)
      Ok(view(controllers.fileUpload.routes.FileUploadController.onPageLoad(waypoints, eventType).url,
        templateDownloadLink, instructionsDownloadLink, eventType))
  }
}
