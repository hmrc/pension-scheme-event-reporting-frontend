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

package pages.event11

import java.time.LocalDate
import controllers.event11.routes
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{Page, QuestionPage, Waypoints}

case object UnAuthPaymentsRuleChangeDatePage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ "event11" \ toString

  override def toString: String = "unAuthPaymentsRuleChangeDate"

  override def route(waypoints: Waypoints): Call =
    routes.UnAuthPaymentsRuleChangeDateController.onPageLoad(waypoints)

   def nextPageNormalMode(waypoints: Waypoints): Page = {
    HasSchemeChangedRulesInvestmentsInAssetsPage
  }
}
