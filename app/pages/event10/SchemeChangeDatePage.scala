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

package pages.event10

import controllers.event10.routes
import models.event10.SchemeChangeDate
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{QuestionPage, Waypoints}

case object SchemeChangeDatePage extends QuestionPage[SchemeChangeDate] {

  override def path: JsPath = JsPath \ "event10" \ toString

  override def toString: String = "schemeChangeDate"

  override def route(waypoints: Waypoints): Call =
    routes.SchemeChangeDateController.onPageLoad(waypoints)
}
