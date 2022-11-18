/*
 * Copyright 2022 HM Revenue & Customs
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

package pages.event1.member

import controllers.event1.member.routes
import models.UserAnswers
import models.enumeration.EventType
import models.event1.member.RefundOfContributions
import pages.common.MembersOrEmployersPage
import pages.event1.PaymentValueAndDatePage
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class RefundOfContributionsPage(index: Int) extends QuestionPage[RefundOfContributions] {

  override def path: JsPath = MembersOrEmployersPage(EventType.Event1)(index) \ toString

  override def toString: String = "refundOfContributions"

  override def route(waypoints: Waypoints): Call = {
    routes.RefundOfContributionsController.onPageLoad(waypoints, index)
  }

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    PaymentValueAndDatePage(index)
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    PaymentValueAndDatePage(index)
  }

}
