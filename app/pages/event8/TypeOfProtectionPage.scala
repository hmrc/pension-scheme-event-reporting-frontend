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

package pages.event8

import controllers.event8.routes
import models.UserAnswers
import models.enumeration.EventType
import models.enumeration.EventType.{Event8, Event8A}
import models.event8.TypeOfProtection
import pages.common.MembersPage
import pages.event8a.Event8ACheckYourAnswersPage
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.{Success, Try}

case class TypeOfProtectionPage(eventType: EventType, index: Int) extends QuestionPage[TypeOfProtection] {

  override def path: JsPath = MembersPage(eventType)(index) \ toString

  override def toString: String = "typeOfProtection"

  override def route(waypoints: Waypoints): Call =
    routes.TypeOfProtectionController.onPageLoad(waypoints, eventType, index)

  override def cleanupBeforeSettingValue(value: Option[TypeOfProtection], userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.get(TypeOfProtectionPage(eventType, index)) match {
      case originalTypeOfProtection@Some(_) if originalTypeOfProtection != value =>
        userAnswers.remove(TypeOfProtectionReferencePage(eventType, index))
      case _ => Success(userAnswers)
    }
  }

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    TypeOfProtectionReferencePage(eventType, index)

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    val originalOptionSelected = originalAnswers.get(TypeOfProtectionPage(eventType, index))
    val updatedOptionSelected = updatedAnswers.get(TypeOfProtectionPage(eventType, index))
    val answerIsChanged = originalOptionSelected != updatedOptionSelected

    (answerIsChanged, eventType) match {
      case (true, Event8) => TypeOfProtectionReferencePage(eventType, index)
      case (false, Event8) => Event8CheckYourAnswersPage(index)
      case (true, Event8A) => TypeOfProtectionReferencePage(eventType, index)
      case (false, Event8A) => Event8ACheckYourAnswersPage(index)
    }
  }
}
