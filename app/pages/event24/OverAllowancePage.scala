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

import controllers.event24.routes
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.common.MembersPage
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{EmptyWaypoints, NonEmptyWaypoints, Page, QuestionPage, Waypoints}

import scala.util.{Success, Try}

case class OverAllowancePage(index: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = MembersPage(EventType.Event24)(index) \ OverAllowancePage.toString

  override def toString: String = "overAllowance"

  override def route(waypoints: Waypoints): Call =
    routes.OverAllowanceController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this).map {
      case true  => MarginalRatePage(index)
      case false => OverAllowanceAndDeathBenefitPage(index)
    }.orRecover
  }

  override def cleanupBeforeSettingValue(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(true) =>
        Success(userAnswers
          .remove(OverAllowanceAndDeathBenefitPage(index))
          .getOrElse(userAnswers)
        )
      case _ => Success(userAnswers)
    }
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    val originalOptionSelected = originalAnswers.get(this)
    val updatedOptionSelected = updatedAnswers.get(this)
    val answerIsChanged = originalOptionSelected != updatedOptionSelected

    if (answerIsChanged) { nextPageNormalMode(EmptyWaypoints, originalAnswers, updatedAnswers) }
    else { Event24CheckYourAnswersPage(index) }
  }
}

object OverAllowancePage {
  override def toString: String = "overAllowance"
}
