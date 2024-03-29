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
import models.event11.Event11Date
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{Page, QuestionPage, Waypoints}

case object InvestmentsInAssetsRuleChangeDatePage extends QuestionPage[Event11Date] {

  override def path: JsPath = JsPath \ "event11" \ toString

  override def toString: String = "investmentsInAssetsRuleChangeDate"

  override def route(waypoints: Waypoints): Call =
    routes.InvestmentsInAssetsRuleChangeDateController.onPageLoad(waypoints)

  def nextPageNormalMode(waypoints: Waypoints): Page = {
    Event11CheckYourAnswersPage()
  }
}
