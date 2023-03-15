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

package pages.event6

import controllers.event6.routes
import models.UserAnswers
import models.enumeration.EventType
import models.event6.TypeOfProtection
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class TypeOfProtectionPage(eventType: EventType, index: Int) extends QuestionPage[TypeOfProtection] {
  override def path: JsPath = JsPath \ s"event${eventType.toString}" \ toString

  override def toString: String = "typeOfProtection"

  def route(waypoints: Waypoints): Call =
    routes.TypeOfProtectionController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    InputProtectionTypePage(eventType, index)

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page =
    InputProtectionTypePage(eventType, index)

}
