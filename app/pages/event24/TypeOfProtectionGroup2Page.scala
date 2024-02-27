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

package pages.event24

import models.enumeration.EventType
import models.event24.TypeOfProtectionGroup2
import models.event24.TypeOfProtectionGroup2.NoOtherProtections
import models.{Index, UserAnswers}
import pages.common.MembersPage
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.{Success, Try}

case class TypeOfProtectionGroup2Page(index: Index) extends QuestionPage[TypeOfProtectionGroup2] {
  override def path: JsPath = MembersPage(EventType.Event24)(index) \ TypeOfProtectionGroup2Page.toString

  override def route(waypoints: Waypoints): Call = {
    controllers.event24.routes.TypeOfProtectionGroup2Controller.onPageLoad(waypoints, index)
  }

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this).map {
      case NoOtherProtections => OverAllowancePage(index)
      case _ => TypeOfProtectionGroup2ReferencePage(index)
    }.orRecover
  }

  override def cleanupBeforeSettingValue(value: Option[TypeOfProtectionGroup2], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(NoOtherProtections) =>
        Success(userAnswers
          .remove(TypeOfProtectionGroup2ReferencePage(index))
          .getOrElse(userAnswers)
        )
      case _ => Success(userAnswers)
    }
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page =
    TypeOfProtectionGroup2ReferencePage(index)
}

object TypeOfProtectionGroup2Page {
  override def toString: String = "typeOfProtectionGroup2"
}
