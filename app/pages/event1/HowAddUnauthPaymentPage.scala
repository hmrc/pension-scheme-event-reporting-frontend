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
import models.event1.HowAddUnauthPayment
import models.event1.HowAddUnauthPayment.Manual
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{MembersOrEmployersPage, Page, QuestionPage, Waypoints}

case class HowAddUnauthPaymentPage(index: Int) extends QuestionPage[HowAddUnauthPayment] {

  override def path: JsPath = MembersOrEmployersPage(index).path \ toString

  override def toString: String = "howAddUnauthPayment"

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this) match {
      case Some(Manual) => WhoReceivedUnauthPaymentPage(index)
      case _ => this
    }

  override def route(waypoints: Waypoints): Call = {
    routes.HowAddUnauthPaymentController.onPageLoad(waypoints, index)
  }
}
