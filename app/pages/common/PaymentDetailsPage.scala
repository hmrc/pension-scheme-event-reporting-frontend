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

package pages.common

import controllers.common.routes
import models.UserAnswers
import models.common.PaymentDetails
import models.enumeration.EventType
import models.enumeration.EventType.Event4
import pages.event4.Event4CheckYourAnswersPage
import pages.event5.Event5CheckYourAnswersPage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class PaymentDetailsPage(eventType: EventType, index: Int) extends QuestionPage[PaymentDetails] {

  override def path: JsPath = MembersPage(eventType)(index) \ PaymentDetailsPage.toString

  override def route(waypoints: Waypoints): Call = {
    routes.PaymentDetailsController.onPageLoad(waypoints, eventType, index)
  }

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    if (eventType == Event4) Event4CheckYourAnswersPage(index) else Event5CheckYourAnswersPage(index)
  }
}

object PaymentDetailsPage {
  override def toString: String = "paymentDetails"
}
