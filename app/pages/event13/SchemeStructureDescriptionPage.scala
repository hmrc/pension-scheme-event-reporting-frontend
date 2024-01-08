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

package pages.event13

import controllers.event13.routes
import models.UserAnswers
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{Page, QuestionPage, Waypoints}

case object SchemeStructureDescriptionPage extends QuestionPage[String] {

  override def path: JsPath = JsPath \ "event13" \ toString

  override def toString: String = "schemeStructureDescription"

  override def route(waypoints: Waypoints): Call =
    routes.SchemeStructureDescriptionController.onPageLoad(waypoints)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    ChangeDatePage
  }
}
