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
import models.enumeration.EventType.{Event1, Event2, Event22, Event23, Event6,  Event7, Event8, Event8A}
import pages.event1.DoYouHoldSignedMandatePage
import pages.event7.LumpSumAmountPage
import pages.event2.AmountPaidPage
import pages.event6.TypeOfProtectionPage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import utils.Event2MemberPageNumbers

case class MembersDetailsPage(eventType: EventType, index: Int, memberPageNo: Int=0) extends QuestionPage[MembersDetails] {

  override def path: JsPath =
    (eventType, memberPageNo) match {
      case (Event2, Event2MemberPageNumbers.FIRST_PAGE_DECEASED) => MembersPage(eventType)(index) \ MembersDetailsPage.toStringEvent2Deceased
      case (Event2, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY) => MembersPage(eventType)(index) \ MembersDetailsPage.toStringEvent2Beneficiary
      case (Event6 | Event7 | Event8 | Event8A | Event22 | Event23, _) => MembersPage(eventType)(index) \ MembersDetailsPage.toString
      case _ => MembersOrEmployersPage(eventType)(index) \ MembersDetailsPage.toString
    }

  override def route(waypoints: Waypoints): Call = controllers.common.routes.MembersDetailsController.onPageLoad(waypoints, eventType, index, memberPageNo)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    (eventType, index, memberPageNo) match {
      case (Event1, index, _) => DoYouHoldSignedMandatePage(index)
      case (Event2, index, 1) => MembersDetailsPage(eventType, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY)
      case (Event2, index, 2) => AmountPaidPage(index, Event2)
      case (Event6, index, _) => TypeOfProtectionPage(eventType, index)
      case (Event7, index, _) => LumpSumAmountPage(index)
      case (Event8, index, _) => pages.event8.TypeOfProtectionPage(eventType, index)
      case (Event8A, index, _) => pages.event8a.PaymentTypePage(eventType, index)
      case (Event22, index, _) => ChooseTaxYearPage(eventType, index)
      case (Event23, index, _) => ChooseTaxYearPage(eventType, index)
      case _ => super.nextPageNormalMode(waypoints, answers)
    }
  }
}

object MembersDetailsPage {
  override def toString: String = "membersDetails"
  private val toStringEvent2Deceased: String = "deceasedMembersDetails"
  private val toStringEvent2Beneficiary: String = "beneficiaryDetails"
}
