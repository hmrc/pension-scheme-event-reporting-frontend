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

package controllers.event11

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType
import pages.Waypoints
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event11.WhatYouWillNeedView

import javax.inject.Inject

class WhatYouWillNeedController @Inject()(val controllerComponents: MessagesControllerComponents,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          view: WhatYouWillNeedView
                                         ) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event11

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)) {
    implicit request =>
      Ok(view(controllers.event11.routes.HasSchemeChangedRulesController.onPageLoad(waypoints).url))
  }
}
