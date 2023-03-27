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

package pages.event8a

import controllers.event8a.routes
import models.enumeration.EventType
import models.event8a.LumpSumDetails
import pages.common.MembersPage
import pages.{QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class LumpSumAmountAndDatePage(eventType: EventType, index: Int) extends QuestionPage[LumpSumDetails] {

  override def path: JsPath = MembersPage(EventType.Event8A)(index) \ LumpSumAmountAndDatePage.toString

  override def route(waypoints: Waypoints): Call =
    routes.LumpSumAmountAndDateController.onPageLoad(waypoints, index)

  //  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
  //    Event8ACheckYourAnswersPage(index)
}

object LumpSumAmountAndDatePage {
  override def toString: String = "lumpSumAmountAndDate"
}