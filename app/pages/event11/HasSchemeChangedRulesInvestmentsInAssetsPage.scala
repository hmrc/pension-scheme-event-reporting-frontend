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

package pages.event11

import controllers.event11.routes
import models.UserAnswers
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.{Success, Try}

case object HasSchemeChangedRulesInvestmentsInAssetsPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "event11" \ toString

  override def toString: String = "hasSchemeChangedRulesInvestmentsInAssets"

  override def route(waypoints: Waypoints): Call =
    routes.HasSchemeChangedRulesInvestmentsInAssetsController.onPageLoad(waypoints)

  override def cleanupBeforeSettingValue(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.get(HasSchemeChangedRulesInvestmentsInAssetsPage) match {
      case originalOption@Some(_) if originalOption != value =>
        userAnswers.remove(InvestmentsInAssetsRuleChangeDatePage)
      case _ => Success(userAnswers)
    }
  }

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this).map {
      case true => InvestmentsInAssetsRuleChangeDatePage
      case false => Event11CheckYourAnswersPage()
    }.orRecover
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    val originalOptionSelected = originalAnswers.get(HasSchemeChangedRulesInvestmentsInAssetsPage)
    val updatedOptionSelected = updatedAnswers.get(HasSchemeChangedRulesInvestmentsInAssetsPage)
    val answerIsChanged = originalOptionSelected != updatedOptionSelected

    (updatedAnswers.get(this), answerIsChanged) match {
      case (Some(true), true) => InvestmentsInAssetsRuleChangeDatePage
      case _ => Event11CheckYourAnswersPage()
    }
  }
}
