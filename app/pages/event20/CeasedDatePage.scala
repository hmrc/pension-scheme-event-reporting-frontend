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

package pages.event20

import java.time.LocalDate
import controllers.event20.routes
import models.UserAnswers
import models.event20.Event20Date
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{IndexPage, Page, QuestionPage, Waypoints}

case object CeasedDatePage extends QuestionPage[Event20Date] {

  override def path: JsPath = JsPath \ "event20" \ toString

  override def toString: String = "ceasedDate"

  override def route(waypoints: Waypoints): Call =
    routes.CeasedDateController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    IndexPage
  }
}
