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

import models.UserAnswers
import models.common.MembersDetails
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event23, Event6, Event8, Event8A}
import pages.event1.DoYouHoldSignedMandatePage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class MembersDetailsPage(eventType: EventType, index: Int) extends QuestionPage[MembersDetails] {

  override def path: JsPath =
    eventType match {
      case Event6 | Event8 | Event8A | Event22 | Event23 => MembersPage(eventType)(index) \ MembersDetailsPage.toString
      case _ => MembersOrEmployersPage(eventType)(index) \ MembersDetailsPage.toString
    }

  override def route(waypoints: Waypoints): Call = controllers.common.routes.MembersDetailsController.onPageLoad(waypoints, eventType, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    (eventType, index) match {
      case (Event1, index) => DoYouHoldSignedMandatePage(index)
      case (Event6, index) => pages.event6.TypeOfProtectionPage(eventType, index)
      case (Event8, index) => pages.event8.TypeOfProtectionPage(eventType, index)
      case (Event8A, index) => pages.event8.TypeOfProtectionPage(eventType, index) //TODO: Change this to eventa's one
      case (Event22, index) => ChooseTaxYearPage(eventType, index)
      case (Event23, index) => ChooseTaxYearPage(eventType, index)
      case _ => super.nextPageNormalMode(waypoints, answers)
    }
  }
}

object MembersDetailsPage {
  override def toString: String = "membersDetails"
}
