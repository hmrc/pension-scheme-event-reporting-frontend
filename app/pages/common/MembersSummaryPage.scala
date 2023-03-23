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

package pages.common

import models.{Index, UserAnswers}
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23, Event6}
import pages.{EventSummaryPage, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class MembersSummaryPage(eventType: EventType, pageNumber: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ s"event${eventType.toString}" \ MembersSummaryPage.toString

  override def route(waypoints: Waypoints): Call =
    controllers.common.routes.MembersSummaryController.onPageLoad(waypoints, eventType)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    (eventType, answers.get(MembersSummaryPage(eventType, pageNumber))) match {
      case (Event6, Some(true)) => ManualOrUploadPage(Event6, answers.countAll(MembersPage(EventType.Event6)))
      case (Event22, Some(true)) => ManualOrUploadPage(Event22, answers.countAll(MembersPage(EventType.Event22)))
      case (Event23, Some(true)) => ManualOrUploadPage(Event23, answers.countAll(MembersPage(EventType.Event23)))
      case _ => EventSummaryPage
    }
  }
}
object MembersSummaryPage {
  override def toString: String = "membersSummary"
}

