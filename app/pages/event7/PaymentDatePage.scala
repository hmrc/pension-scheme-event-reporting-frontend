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

package pages.event7

import controllers.event7.routes
import models.UserAnswers
import models.enumeration.EventType
import models.event7.PaymentDate
import pages.common.MembersPage
import pages.{IndexPage, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class PaymentDatePage(index: Int) extends QuestionPage[PaymentDate] {
  override def path: JsPath = MembersPage(EventType.Event7)(index) \ PaymentDatePage.toString

  override def route(waypoints: Waypoints): Call =
    routes.PaymentDateController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    IndexPage
    /*Event7CheckYourAnswersPage(index)*/
}

object PaymentDatePage {
  override def toString: String = "PaymentDate"
}
