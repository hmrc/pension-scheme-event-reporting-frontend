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

package pages.event12

import controllers.event12.routes
import models.UserAnswers
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.RecoveryOps

import scala.util.{Success, Try}

case object HasSchemeChangedRulesPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "event12" \ toString

  override def toString: String = "hasSchemeChangedRules"

  override def cleanupBeforeSettingValue(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.get(HasSchemeChangedRulesPage) match {
      case originalOption@Some(_) if originalOption != value =>
        userAnswers.remove(DateOfChangePage)
      case _ => Success(userAnswers)
    }
  }

  override def route(waypoints: Waypoints): Call =
    routes.HasSchemeChangedRulesController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this).map {
      case true => DateOfChangePage
      case false => CannotSubmitPage
    }.orRecover
  }
}
