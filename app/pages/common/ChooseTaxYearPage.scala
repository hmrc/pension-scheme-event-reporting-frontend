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

package pages.common

import controllers.common.routes
import models.{Index, UserAnswers}
import models.common.ChooseTaxYear
import models.enumeration.EventType
import models.enumeration.EventType.Event23
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class ChooseTaxYearPage(eventType: EventType, index: Index) extends QuestionPage[ChooseTaxYear] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "chooseTaxYear"

  override def route(waypoints: Waypoints): Call =
    routes.ChooseTaxYearController.onPageLoad(waypoints, eventType)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    TotalPensionAmountsPage(Event23, index)
}
