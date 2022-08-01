/*
 * Copyright 2022 HM Revenue & Customs
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

package pages.event18

import controllers.event18.routes
import models.UserAnswers
import models.event18.Event18Confirmation
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{Page, QuestionPage, Waypoints}

case object Event18ConfirmationPage extends QuestionPage[Set[Event18Confirmation]] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "event18Confirmation"

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = super.nextPageNormalMode(waypoints, answers)

  override def route(waypoints: Waypoints): Call =
    routes.Event18ConfirmationController.onPageLoad(waypoints)
}
