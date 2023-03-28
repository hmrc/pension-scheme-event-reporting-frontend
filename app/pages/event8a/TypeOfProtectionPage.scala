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
import models.event8a.TypeOfProtection
import models.{Index, UserAnswers}
import pages.common.MembersPage
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.{Success, Try}

case class TypeOfProtectionPage(eventType: EventType, index: Index) extends QuestionPage[TypeOfProtection] {

  override def path: JsPath = MembersPage(EventType.Event8A)(index) \ toString

  override def toString: String = "typeOfProtection"

  override def route(waypoints: Waypoints): Call =
    routes.TypeOfProtectionController.onPageLoad(waypoints, index)

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

    answerIsChanged match {
      case true => TypeOfProtectionReferencePage(eventType, index)
      case _ => Event8ACheckYourAnswersPage(index)
    }
  }
}
