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

import controllers.common.routes
import models.{Index, UserAnswers}
import play.api.libs.json.JsPath
import models.enumeration.EventType
import models.enumeration.EventType.Event1
import pages.event1.UnauthPaymentSummaryPage
import play.api.mvc.Call
import pages.{Page, QuestionPage, Waypoints}

case class RemoveMemberPage(eventType: EventType, index: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "removeMember"

  override def route(waypoints: Waypoints): Call =
    routes.RemoveMemberController.onPageLoad(waypoints, eventType, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    // TODO: refactor
    answers.get(this).map {
      case true  => if (eventType == Event1) UnauthPaymentSummaryPage else MembersSummaryPage(eventType, index)
      case false => if (eventType == Event1) UnauthPaymentSummaryPage else MembersSummaryPage(eventType, index)
    }.orRecover
  }
}
