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

package pages.event25

import play.api.mvc.Call
import pages.{Page, Waypoints}
import controllers.event25.routes
import models.{Index, UserAnswers}
import models.enumeration.EventType
import pages.common.{ManualOrUploadPage, MembersDetailsPage}

case class WhatYouWillNeedPage(index: Index) extends Page {

  override def route(waypoints: Waypoints): Call =
    routes.WhatYouWillNeedController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    ManualOrUploadPage(EventType.Event25, index)
}
