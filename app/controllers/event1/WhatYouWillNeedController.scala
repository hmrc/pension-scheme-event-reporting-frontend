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

package controllers.event1

import controllers.actions._
import models.Index
import models.enumeration.EventType
import models.enumeration.EventType.Event1
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event1.WhatYouWillNeedView

import javax.inject.Inject

class WhatYouWillNeedController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: WhatYouWillNeedView
                                         ) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event1

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) {
    implicit request =>
      Ok(view(controllers.common.routes.MembersDetailsController.onPageLoad(waypoints, Event1, index).url))
  }
}
