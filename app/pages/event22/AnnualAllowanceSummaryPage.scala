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

package pages.event22

import controllers.event22.routes
import models.UserAnswers
import models.enumeration.EventType
import pages.common.{MembersOrEmployersPage, MembersPage}
import pages.{IndexPage, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object AnnualAllowanceSummaryPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "annualAllowanceSummary"

  override def route(waypoints: Waypoints): Call =
    routes.AnnualAllowanceSummaryController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {

    answers.get(AnnualAllowanceSummaryPage) match {
      case Some(true) => HowAddAnnualAllowancePage(answers.countAll(MembersPage(EventType.Event22)))
      case _ => IndexPage
    }

  }
}
