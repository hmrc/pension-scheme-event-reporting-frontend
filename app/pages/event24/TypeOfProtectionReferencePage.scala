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

import models.UserAnswers
import models.enumeration.EventType
import pages.common.MembersPage
import pages.{EmptyWaypoints, NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class TypeOfProtectionReferencePage(index: Int) extends QuestionPage[String] {

  override def path: JsPath = MembersPage(EventType.Event24)(index) \ TypeOfProtectionReferencePage.toString

  override def route(waypoints: Waypoints): Call =
    controllers.event24.routes.TypeOfProtectionReferenceController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    OverAllowancePage(index)

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    val originalOptionSelected = originalAnswers.get(this)
    val updatedOptionSelected = updatedAnswers.get(this)
    val answerIsChanged = originalOptionSelected != updatedOptionSelected

    if (answerIsChanged) {
      nextPageNormalMode(EmptyWaypoints, updatedAnswers)
    }
    else {
      Event24CheckYourAnswersPage(index)
    }
  }
}

object TypeOfProtectionReferencePage {
  override def toString: String = "typeOfProtectionReference"
}