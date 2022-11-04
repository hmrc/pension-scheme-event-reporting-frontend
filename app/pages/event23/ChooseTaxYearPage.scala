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

package pages.event23

import controllers.event23.routes
import models.event23.ChooseTaxYear
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{Waypoints, QuestionPage}

case object ChooseTaxYearPage extends QuestionPage[ChooseTaxYear] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "chooseTaxYear"

  override def route(waypoints: Waypoints): Call =
    routes.ChooseTaxYearController.onPageLoad(waypoints)
}