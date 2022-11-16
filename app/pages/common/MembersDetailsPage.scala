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

import models.UserAnswers
import models.common.MembersDetails
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event23}
import pages.event1.{DoYouHoldSignedMandatePage, MembersOrEmployersPage}
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class MembersDetailsPage(eventType: EventType, index: Int) extends QuestionPage[MembersDetails] {

  override def path: JsPath = MembersOrEmployersPage(index) \ MembersDetailsPage.toString

  override def route(waypoints: Waypoints): Call = controllers.common.routes.MembersDetailsController.onPageLoadWithIndex(waypoints, eventType, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    (eventType, index) match {
      case (Event1, index) => DoYouHoldSignedMandatePage(index)
      case (Event23, index) => ChooseTaxYearPage(eventType, index)
      case _ => super.nextPageNormalMode(waypoints, answers)
    }
  }
}

object MembersDetailsPage {
  override def toString: String = "membersDetails"
}
