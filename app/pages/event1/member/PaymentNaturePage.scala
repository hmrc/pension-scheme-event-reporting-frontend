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

package pages.event1.member

import controllers.event1.routes
import models.UserAnswers
import models.enumeration.AddressJourneyType.Event1MemberPropertyAddressJourney
import models.enumeration.EventType
import models.event1.PaymentNature
import models.event1.PaymentNature._
import pages.common.MembersOrEmployersPage
import pages.{JourneyRecoveryPage, NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class PaymentNaturePage(index: Int) extends QuestionPage[PaymentNature] {

  override def path: JsPath = MembersOrEmployersPage(EventType.Event1)(index) \ toString

  override def toString: String = "paymentNatureMember"

  def route(waypoints: Waypoints): Call =
    routes.PaymentNatureController.onPageLoad(waypoints, index)

  // scalastyle:off
  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(PaymentNaturePage(index)) match {
      case Some(BenefitInKind) => BenefitInKindBriefDescriptionPage(index)
      case Some(TransferToNonRegPensionScheme) => pages.event1.member.WhoWasTheTransferMadePage(index)
      case Some(ErrorCalcTaxFreeLumpSums) => pages.event1.member.ErrorDescriptionPage(index)
      case Some(BenefitsPaidEarly) => pages.event1.member.BenefitsPaidEarlyPage(index)
      case Some(RefundOfContributions) => pages.event1.member.RefundOfContributionsPage(index)
      case Some(OverpaymentOrWriteOff) => pages.event1.member.ReasonForTheOverpaymentOrWriteOffPage(index)
      case Some(ResidentialPropertyHeld) => pages.address.EnterPostcodePage(Event1MemberPropertyAddressJourney, index)
      case Some(TangibleMoveablePropertyHeld) => MemberTangibleMoveablePropertyPage(index)
      case Some(CourtOrConfiscationOrder) => pages.event1.member.UnauthorisedPaymentRecipientNamePage(index)
      case Some(MemberOther) => MemberPaymentNatureDescriptionPage(index)
      case _ => JourneyRecoveryPage
    }
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    updatedAnswers.get(PaymentNaturePage(index)) match {
      case Some(BenefitInKind) => BenefitInKindBriefDescriptionPage(index)
      case Some(TransferToNonRegPensionScheme) => pages.event1.member.WhoWasTheTransferMadePage(index)
      case Some(ErrorCalcTaxFreeLumpSums) => pages.event1.member.ErrorDescriptionPage(index)
      case Some(BenefitsPaidEarly) => pages.event1.member.BenefitsPaidEarlyPage(index)
      case Some(RefundOfContributions) => pages.event1.member.RefundOfContributionsPage(index)
      case Some(OverpaymentOrWriteOff) => pages.event1.member.ReasonForTheOverpaymentOrWriteOffPage(index)
      case Some(ResidentialPropertyHeld) => pages.address.EnterPostcodePage(Event1MemberPropertyAddressJourney, index)
      case Some(TangibleMoveablePropertyHeld) => MemberTangibleMoveablePropertyPage(index)
      case Some(CourtOrConfiscationOrder) => pages.event1.member.UnauthorisedPaymentRecipientNamePage(index)
      case Some(MemberOther) => MemberPaymentNatureDescriptionPage(index)
      case _ => JourneyRecoveryPage
    }
  }

}
