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

package pages.event10

import controllers.event10.routes
import models.UserAnswers
import models.event10.BecomeOrCeaseScheme
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.{Success, Try}

case object BecomeOrCeaseSchemePage extends QuestionPage[BecomeOrCeaseScheme] {

  override def path: JsPath = JsPath \ "event10" \ toString

  override def toString: String = "becomeOrCeaseScheme"

  override def cleanupBeforeSettingValue(value: Option[BecomeOrCeaseScheme], userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.get(BecomeOrCeaseSchemePage) match {
      case originalOption@Some(_) if originalOption != value =>
        userAnswers.removeOrException(BecomeOrCeaseSchemePage)
        userAnswers.remove(ContractsOrPoliciesPage)
      case _ => Success(userAnswers)
    }
  }

  override def route(waypoints: Waypoints): Call =
    routes.BecomeOrCeaseSchemeController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    SchemeChangeDatePage

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    val originalOptionSelected = originalAnswers.get(BecomeOrCeaseSchemePage)
    val updatedOptionSelected = updatedAnswers.get(BecomeOrCeaseSchemePage)
    val answerIsChanged = originalOptionSelected != updatedOptionSelected

    if (answerIsChanged) SchemeChangeDatePage else Event10CheckYourAnswersPage()

  }
}
