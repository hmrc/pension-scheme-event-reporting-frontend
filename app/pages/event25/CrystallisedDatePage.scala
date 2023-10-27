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

import controllers.event25.routes
import models.UserAnswers
import models.enumeration.EventType
import models.event25.CrystallisedDate
import pages.common.MembersPage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class CrystallisedDatePage(index: Int) extends QuestionPage[CrystallisedDate] {
  override def path: JsPath = MembersPage(EventType.Event25)(index) \ CrystallisedDatePage.toString

  override def route(waypoints: Waypoints): Call =
    routes.CrystallisedDateController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    BCETypeSelectionPage(index)
}

object CrystallisedDatePage {
  override def toString: String = "crystallisedDate"
}
