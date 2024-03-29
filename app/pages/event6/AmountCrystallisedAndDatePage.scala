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

package pages.event6

import controllers.event6.routes
import models.UserAnswers
import models.enumeration.EventType
import models.event6.CrystallisedDetails
import pages.common.MembersPage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class AmountCrystallisedAndDatePage(eventType: EventType, index: Int) extends QuestionPage[CrystallisedDetails] {
  override def path: JsPath = MembersPage(EventType.Event6)(index) \ AmountCrystallisedAndDatePage.toString

  override def route(waypoints: Waypoints): Call =
    routes.AmountCrystallisedAndDateController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    Event6CheckYourAnswersPage(index)
}

object AmountCrystallisedAndDatePage {
  override def toString: String = "AmountCrystallisedAndDate"
}
