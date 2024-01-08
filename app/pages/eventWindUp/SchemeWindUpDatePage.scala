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

package pages.eventWindUp

import controllers.eventWindUp.routes
import models.UserAnswers
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import java.time.LocalDate

case object SchemeWindUpDatePage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ "eventWindUp" \ toString

  override def toString: String = "schemeWindUpDate"

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this) match {
      case Some(_) => EventWindUpCheckYourAnswersPage
      case _ => this
    }

  override def route(waypoints: Waypoints): Call =
    routes.SchemeWindUpDateController.onPageLoad(waypoints)
}
