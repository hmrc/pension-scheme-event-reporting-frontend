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

package pages.event2

import java.time.LocalDate
import controllers.event2.routes
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.common.MembersPage
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{Page, QuestionPage, Waypoints}

case class DatePaidPage(index:Index, eventType: EventType) extends QuestionPage[LocalDate] {

  override def path: JsPath = MembersPage(eventType)(index) \ toString

  override def toString: String = "datePaid"

  override def route(waypoints: Waypoints): Call =
    routes.DatePaidController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    Event2CheckYourAnswersPage(index)
  }
}
