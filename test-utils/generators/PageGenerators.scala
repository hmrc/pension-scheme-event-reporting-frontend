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

package generators

import org.scalacheck.Arbitrary
import pages._
import pages.common.MembersDetailsPage
import pages.event1.employer.{EmployerPaymentNatureDescriptionPage, EmployerTangibleMoveablePropertyPage, UnauthorisedPaymentRecipientNamePage}
import pages.event1.member._
import pages.event1.{employer, member}
import pages.eventWindUp.SchemeWindUpDatePage

trait PageGenerators {

  implicit lazy val arbitraryHowAddDualAllowancePage: Arbitrary[event23.HowAddDualAllowancePage.type] =
    Arbitrary(event23.HowAddDualAllowancePage)

  implicit lazy val arbitraryPaymentValueAndDatePage: Arbitrary[event1.PaymentValueAndDatePage.type] =
    Arbitrary(event1.PaymentValueAndDatePage)

  implicit lazy val arbitraryLoanDetailsPage: Arbitrary[event1.employer.LoanDetailsPage.type] =
    Arbitrary(event1.employer.LoanDetailsPage)

  implicit lazy val arbitraryEmployerPaymentNatureDescriptionPage: Arbitrary[EmployerPaymentNatureDescriptionPage.type] =
    Arbitrary(employer.EmployerPaymentNatureDescriptionPage)

  implicit lazy val arbitraryMemberPaymentNatureDescriptionPage: Arbitrary[MemberPaymentNatureDescriptionPage.type] =
    Arbitrary(member.MemberPaymentNatureDescriptionPage)

  implicit lazy val arbitraryEmployerTangibleMoveablePropertyPage: Arbitrary[EmployerTangibleMoveablePropertyPage.type] =
    Arbitrary(employer.EmployerTangibleMoveablePropertyPage)

  implicit lazy val arbitraryMemberTangibleMoveablePropertyPage: Arbitrary[MemberTangibleMoveablePropertyPage.type] =
    Arbitrary(member.MemberTangibleMoveablePropertyPage)

  implicit lazy val arbitraryMemberUnauthorisedPaymentRecipientNamePage: Arbitrary[event1.member.UnauthorisedPaymentRecipientNamePage.type] =
    Arbitrary(event1.member.UnauthorisedPaymentRecipientNamePage)

  implicit lazy val arbitraryUnauthorisedPaymentRecipientNamePage: Arbitrary[UnauthorisedPaymentRecipientNamePage.type] =
    Arbitrary(employer.UnauthorisedPaymentRecipientNamePage)

  implicit lazy val arbitraryReasonForTheOverpaymentOrWriteOffPage: Arbitrary[ReasonForTheOverpaymentOrWriteOffPage.type] =
    Arbitrary(member.ReasonForTheOverpaymentOrWriteOffPage)

  implicit lazy val arbitraryRefundOfContributionsPage: Arbitrary[RefundOfContributionsPage.type] =
    Arbitrary(member.RefundOfContributionsPage)

  implicit lazy val arbitrarySchemeDetailsPage: Arbitrary[SchemeDetailsPage.type] =
    Arbitrary(event1.member.SchemeDetailsPage)

  implicit lazy val arbitraryWhoWasTheTransferMadePage: Arbitrary[WhoWasTheTransferMadePage.type] =
    Arbitrary(event1.member.WhoWasTheTransferMadePage)

  implicit lazy val arbitraryErrorDescriptionPage: Arbitrary[event1.member.ErrorDescriptionPage.type] =
    Arbitrary(event1.member.ErrorDescriptionPage)

  implicit lazy val arbitraryBenefitsPaidEarlyPage: Arbitrary[BenefitsPaidEarlyPage.type] =
    Arbitrary(member.BenefitsPaidEarlyPage)

  implicit lazy val arbitraryEmployerPaymentNaturePage: Arbitrary[event1.employer.PaymentNaturePage.type] =
    Arbitrary(event1.employer.PaymentNaturePage)

  implicit lazy val arbitraryChooseAddressPage: Arbitrary[address.ChooseAddressPage.type] =
    Arbitrary(address.ChooseAddressPage)

  implicit lazy val arbitraryEnterPostcodePage: Arbitrary[address.EnterPostcodePage.type] =
    Arbitrary(address.EnterPostcodePage)

  implicit lazy val arbitraryCompanyDetailsPage: Arbitrary[event1.employer.CompanyDetailsPage.type] =
    Arbitrary(event1.employer.CompanyDetailsPage)


  implicit lazy val arbitraryBenefitInKindBriefDescriptionPage: Arbitrary[BenefitInKindBriefDescriptionPage.type] =
    Arbitrary(member.BenefitInKindBriefDescriptionPage)

  implicit lazy val arbitrarySchemeUnAuthPaySurchargeMemberPage: Arbitrary[event1.SchemeUnAuthPaySurchargeMemberPage.type] =
    Arbitrary(event1.SchemeUnAuthPaySurchargeMemberPage)

  implicit lazy val arbitraryValueOfUnauthorisedPaymentPage: Arbitrary[event1.ValueOfUnauthorisedPaymentPage.type] =
    Arbitrary(event1.ValueOfUnauthorisedPaymentPage)

  implicit lazy val arbitraryDoYouHoldSignedMandatePage: Arbitrary[event1.DoYouHoldSignedMandatePage.type] =
    Arbitrary(event1.DoYouHoldSignedMandatePage)

  implicit lazy val arbitraryMembersDetailsPage: Arbitrary[MembersDetailsPage.type] =
    Arbitrary(MembersDetailsPage)

  implicit lazy val arbitraryWhoReceivedUnauthPaymentPage: Arbitrary[event1.WhoReceivedUnauthPaymentPage.type] =
    Arbitrary(event1.WhoReceivedUnauthPaymentPage)

  implicit lazy val arbitraryHowAddUnauthPaymentPage: Arbitrary[event1.HowAddUnauthPaymentPage.type] =
    Arbitrary(event1.HowAddUnauthPaymentPage)

  implicit lazy val arbitraryPaymentNaturePage: Arbitrary[PaymentNaturePage.type] =
    Arbitrary(member.PaymentNaturePage)

  implicit lazy val arbitrarySchemeWindUpDatePage: Arbitrary[SchemeWindUpDatePage.type] =
    Arbitrary(SchemeWindUpDatePage)

  implicit lazy val arbitraryEvent18ConfirmationPage: Arbitrary[event18.Event18ConfirmationPage.type] =
    Arbitrary(event18.Event18ConfirmationPage)

  implicit lazy val arbitraryEventSummaryPage: Arbitrary[EventSummaryPage.type] =
    Arbitrary(EventSummaryPage)

  implicit lazy val arbitraryeventSelectionPage: Arbitrary[EventSelectionPage.type] =
    Arbitrary(EventSelectionPage)
}
