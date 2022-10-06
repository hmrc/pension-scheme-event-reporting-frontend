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
import pages.event1.employer.UnauthorisedPaymentRecipientNamePage
import pages.event1.member.{BenefitsPaidEarlyPage, ReasonForTheOverpaymentOrWriteOffPage, RefundOfContributionsPage}
import pages.event1.{employer, member}
import pages.eventWindUp.SchemeWindUpDatePage

trait PageGenerators {

  implicit lazy val arbitraryMemberUnauthorisedPaymentRecipientNamePage: Arbitrary[event1.member.UnauthorisedPaymentRecipientNamePage.type] =
    Arbitrary(event1.member.UnauthorisedPaymentRecipientNamePage)

  implicit lazy val arbitraryUnauthorisedPaymentRecipientNamePage: Arbitrary[UnauthorisedPaymentRecipientNamePage.type] =
    Arbitrary(employer.UnauthorisedPaymentRecipientNamePage)

  implicit lazy val arbitraryReasonForTheOverpaymentOrWriteOffPage: Arbitrary[ReasonForTheOverpaymentOrWriteOffPage.type] =
    Arbitrary(member.ReasonForTheOverpaymentOrWriteOffPage)

  implicit lazy val arbitraryRefundOfContributionsPage: Arbitrary[RefundOfContributionsPage.type] =
    Arbitrary(member.RefundOfContributionsPage)

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


  implicit lazy val arbitraryBenefitInKindBriefDescriptionPage: Arbitrary[event1.BenefitInKindBriefDescriptionPage.type] =
    Arbitrary(event1.BenefitInKindBriefDescriptionPage)

  implicit lazy val arbitrarySchemeUnAuthPaySurchargeMemberPage: Arbitrary[event1.SchemeUnAuthPaySurchargeMemberPage.type] =
    Arbitrary(event1.SchemeUnAuthPaySurchargeMemberPage)

  implicit lazy val arbitraryValueOfUnauthorisedPaymentPage: Arbitrary[event1.ValueOfUnauthorisedPaymentPage.type] =
    Arbitrary(event1.ValueOfUnauthorisedPaymentPage)

  implicit lazy val arbitraryDoYouHoldSignedMandatePage: Arbitrary[event1.DoYouHoldSignedMandatePage.type] =
    Arbitrary(event1.DoYouHoldSignedMandatePage)

  implicit lazy val arbitraryMembersDetailsPage: Arbitrary[event1.MembersDetailsPage.type] =
    Arbitrary(event1.MembersDetailsPage)

  implicit lazy val arbitraryWhoReceivedUnauthPaymentPage: Arbitrary[event1.WhoReceivedUnauthPaymentPage.type] =
    Arbitrary(event1.WhoReceivedUnauthPaymentPage)

  implicit lazy val arbitraryHowAddUnauthPaymentPage: Arbitrary[event1.HowAddUnauthPaymentPage.type] =
    Arbitrary(event1.HowAddUnauthPaymentPage)

  implicit lazy val arbitraryPaymentNaturePage: Arbitrary[event1.PaymentNaturePage.type] =
    Arbitrary(event1.PaymentNaturePage)

  implicit lazy val arbitrarySchemeWindUpDatePage: Arbitrary[SchemeWindUpDatePage.type] =
    Arbitrary(SchemeWindUpDatePage)

  implicit lazy val arbitraryEvent18ConfirmationPage: Arbitrary[event18.Event18ConfirmationPage.type] =
    Arbitrary(event18.Event18ConfirmationPage)

  implicit lazy val arbitraryEventSummaryPage: Arbitrary[EventSummaryPage.type] =
    Arbitrary(EventSummaryPage)

  implicit lazy val arbitraryeventSelectionPage: Arbitrary[EventSelectionPage.type] =
    Arbitrary(EventSelectionPage)

  implicit lazy val arbitraryTestIntPagePage: Arbitrary[TestIntPagePage.type] =
    Arbitrary(TestIntPagePage)

  implicit lazy val arbitraryTestStringPagePage: Arbitrary[TestStringPagePage.type] =
    Arbitrary(TestStringPagePage)

  implicit lazy val arbitraryTestRadioButtonPage: Arbitrary[TestRadioButtonPage.type] =
    Arbitrary(TestRadioButtonPage)

  implicit lazy val arbitraryTestCheckBoxPage: Arbitrary[TestCheckBoxPage.type] =
    Arbitrary(TestCheckBoxPage)

  implicit lazy val arbitraryDatePage: Arbitrary[TestDatePage.type] =
    Arbitrary(TestDatePage)
}
