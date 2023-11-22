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

package pages.event24

import controllers.event24.routes
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.common.MembersPage
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{EmptyWaypoints, NonEmptyWaypoints, Page, QuestionPage, Waypoints}

case class ValidProtectionPage(index: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = MembersPage(EventType.Event24)(index) \ ValidProtectionPage.toString

  override def route(waypoints: Waypoints): Call =
    routes.ValidProtectionController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this).map {
      case true => TypeOfProtectionPage(index)
      case false => OverAllowancePage(index)
    }.orRecover
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    val originalOptionSelected = originalAnswers.get(this)
    val updatedOptionSelected = updatedAnswers.get(this)
    val answerIsChanged = originalOptionSelected != updatedOptionSelected

    if (answerIsChanged) { nextPageNormalMode(EmptyWaypoints, updatedAnswers) }
    else { Event24CheckYourAnswersPage(index) }
  }
}

object ValidProtectionPage {
  override def toString: String = "validProtection"
}
