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
import pages.event1.MembersOrEmployersPage.readsMemberOrEmployerValue
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object UnauthPaymentSummaryPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "unauthPaymentSummary"

  override def route(waypoints: Waypoints): Call =
    routes.UnauthPaymentSummaryController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(UnauthPaymentSummaryPage) match {
      case Some(true) => WhoReceivedUnauthPaymentPage(answers.countAll(MembersOrEmployersPage))
      case _ => UnauthPaymentAndSanctionChargesPage
    }
  }
}

