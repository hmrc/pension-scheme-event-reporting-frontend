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

package pages.event1

import controllers.event1.routes
import models.UserAnswers
import models.enumeration.EventType
import models.event1.PaymentDetails
import pages.{CheckYourAnswersPage, MembersOrEmployersPage, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class PaymentValueAndDatePage(index: Int) extends QuestionPage[PaymentDetails] {

  override def path: JsPath = MembersOrEmployersPage(index).path \ toString

  override def toString: String = "paymentValueAndDate"

  override def route(waypoints: Waypoints): Call =
    routes.PaymentValueAndDateController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    CheckYourAnswersPage(EventType.Event1, Some(index))
}
