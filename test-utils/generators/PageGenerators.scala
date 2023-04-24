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

package generators

import models.Index
import models.enumeration.AddressJourneyType
import models.enumeration.EventType.{Event1, Event2}
import org.scalacheck.Arbitrary
import pages.common.MembersDetailsPage
import pages.event1.employer.{EmployerPaymentNatureDescriptionPage, EmployerTangibleMoveablePropertyPage, UnauthorisedPaymentRecipientNamePage}
import pages.event1.member._
import pages.event1.{employer, member}
import pages.event13.SchemeStructurePage
import pages.event3.EarlyBenefitsBriefDescriptionPage
import pages.eventWindUp.SchemeWindUpDatePage
import pages.{event13, _}

trait PageGenerators {

  implicit lazy val arbitraryFileUploadPage: Arbitrary[fileUpload.FileUploadPage.type] =
    Arbitrary(fileUpload.FileUploadPage)

  implicit lazy val arbitrarydatePaidPage: Arbitrary[event2.DatePaidPage] =
    Arbitrary(event2.DatePaidPage(Index(0), Event2))

  implicit lazy val arbitraryAmountPaidPage: Arbitrary[event2.AmountPaidPage] =
    Arbitrary(event2.AmountPaidPage(Index(0), Event2))

  implicit lazy val arbitraryEarlyBenefitsBriefDescriptionPage: Arbitrary[EarlyBenefitsBriefDescriptionPage] =
    Arbitrary(event3.EarlyBenefitsBriefDescriptionPage(Index(0)))

  implicit lazy val arbitraryReasonForBenefitsPage: Arbitrary[event3.ReasonForBenefitsPage] =
    Arbitrary(event3.ReasonForBenefitsPage(Index(0)))

  implicit lazy val arbitraryCrystallisedAmountPage: Arbitrary[event7.CrystallisedAmountPage] =
    Arbitrary(event7.CrystallisedAmountPage(Index(0)))

  implicit lazy val arbitraryLumpSumAmountPage: Arbitrary[event7.LumpSumAmountPage] =
    Arbitrary(event7.LumpSumAmountPage(Index(0)))

  implicit lazy val arbitraryPaymentTypePage: Arbitrary[event8a.PaymentTypePage.type] =
    Arbitrary(event8a.PaymentTypePage)

  implicit lazy val arbitraryLumpSumAmountAndDatePage: Arbitrary[event8.LumpSumAmountAndDatePage.type] =
    Arbitrary(event8.LumpSumAmountAndDatePage)

  implicit lazy val arbitraryTypeOfProtectionReferencePage: Arbitrary[event8.TypeOfProtectionReferencePage.type] =
    Arbitrary(event8.TypeOfProtectionReferencePage)

  implicit lazy val arbitraryTypeOfProtectionPage: Arbitrary[event8.TypeOfProtectionPage.type] =
    Arbitrary(event8.TypeOfProtectionPage)

  implicit lazy val arbitraryInputProtectionTypePage: Arbitrary[event6.InputProtectionTypePage.type] =
    Arbitrary(event6.InputProtectionTypePage)

  implicit lazy val arbitrarySchemeStructureDescriptionPage: Arbitrary[event13.SchemeStructureDescriptionPage.type] =
    Arbitrary(event13.SchemeStructureDescriptionPage)

  implicit lazy val arbitraryChangeDatePage: Arbitrary[event13.ChangeDatePage.type] =
    Arbitrary(event13.ChangeDatePage)

  implicit lazy val arbitrarySchemeStructurePage: Arbitrary[SchemeStructurePage.type] =
    Arbitrary(event13.SchemeStructurePage)

  implicit lazy val arbitraryTaxYearPage: Arbitrary[TaxYearPage.type] =
    Arbitrary(TaxYearPage)

  implicit lazy val arbitraryManualOrUploadPage: Arbitrary[common.ManualOrUploadPage] =
    Arbitrary(common.ManualOrUploadPage(Event1, Index(0)))

  implicit lazy val arbitraryRemoveEvent18Page: Arbitrary[event18.RemoveEvent18Page.type] =
    Arbitrary(event18.RemoveEvent18Page)

  implicit lazy val arbitraryUnauthPaymentSummaryPage: Arbitrary[event1.UnauthPaymentSummaryPage.type] =
    Arbitrary(event1.UnauthPaymentSummaryPage)

  implicit lazy val arbitraryUnauthPaymentAndSanctionChargesPage: Arbitrary[event1.UnauthPaymentAndSanctionChargesPage.type] =
    Arbitrary(event1.UnauthPaymentAndSanctionChargesPage)

  implicit lazy val arbitraryPaymentValueAndDatePage: Arbitrary[event1.PaymentValueAndDatePage] =
    Arbitrary(event1.PaymentValueAndDatePage(Index(0)))

  implicit lazy val arbitraryLoanDetailsPage: Arbitrary[event1.employer.LoanDetailsPage] =
    Arbitrary(event1.employer.LoanDetailsPage(Index(0)))

  implicit lazy val arbitraryEmployerPaymentNatureDescriptionPage: Arbitrary[EmployerPaymentNatureDescriptionPage] =
    Arbitrary(employer.EmployerPaymentNatureDescriptionPage(Index(0)))

  implicit lazy val arbitraryMemberPaymentNatureDescriptionPage: Arbitrary[MemberPaymentNatureDescriptionPage] =
    Arbitrary(member.MemberPaymentNatureDescriptionPage(Index(0)))

  implicit lazy val arbitraryEmployerTangibleMoveablePropertyPage: Arbitrary[EmployerTangibleMoveablePropertyPage] =
    Arbitrary(employer.EmployerTangibleMoveablePropertyPage(Index(0)))

  implicit lazy val arbitraryMemberTangibleMoveablePropertyPage: Arbitrary[MemberTangibleMoveablePropertyPage] =
    Arbitrary(member.MemberTangibleMoveablePropertyPage(Index(0)))

  implicit lazy val arbitraryMemberUnauthorisedPaymentRecipientNamePage: Arbitrary[event1.member.UnauthorisedPaymentRecipientNamePage] =
    Arbitrary(event1.member.UnauthorisedPaymentRecipientNamePage(Index(0)))

  implicit lazy val arbitraryUnauthorisedPaymentRecipientNamePage: Arbitrary[UnauthorisedPaymentRecipientNamePage] =
    Arbitrary(employer.UnauthorisedPaymentRecipientNamePage(Index(0)))

  implicit lazy val arbitraryReasonForTheOverpaymentOrWriteOffPage: Arbitrary[ReasonForTheOverpaymentOrWriteOffPage] =
    Arbitrary(member.ReasonForTheOverpaymentOrWriteOffPage(Index(0)))

  implicit lazy val arbitraryRefundOfContributionsPage: Arbitrary[RefundOfContributionsPage] =
    Arbitrary(member.RefundOfContributionsPage(Index(0)))

  implicit lazy val arbitrarySchemeDetailsPage: Arbitrary[SchemeDetailsPage] =
    Arbitrary(event1.member.SchemeDetailsPage(Index(0)))

  implicit lazy val arbitraryWhoWasTheTransferMadePage: Arbitrary[WhoWasTheTransferMadePage] =
    Arbitrary(event1.member.WhoWasTheTransferMadePage(Index(0)))

  implicit lazy val arbitraryErrorDescriptionPage: Arbitrary[event1.member.ErrorDescriptionPage] =
    Arbitrary(event1.member.ErrorDescriptionPage(Index(0)))

  implicit lazy val arbitraryBenefitsPaidEarlyPage: Arbitrary[BenefitsPaidEarlyPage] =
    Arbitrary(member.BenefitsPaidEarlyPage(Index(0)))

  implicit lazy val arbitraryEmployerPaymentNaturePage: Arbitrary[event1.employer.PaymentNaturePage] =
    Arbitrary(event1.employer.PaymentNaturePage(Index(0)))

  implicit lazy val arbitraryChooseAddressPage: Arbitrary[address.ChooseAddressPage] =
    Arbitrary(address.ChooseAddressPage(AddressJourneyType.Event1EmployerAddressJourney, (0)))

  implicit lazy val arbitraryEnterPostcodePage: Arbitrary[address.EnterPostcodePage] =
    Arbitrary(address.EnterPostcodePage(AddressJourneyType.Event1EmployerAddressJourney, Index(0)))

  implicit lazy val arbitraryCompanyDetailsPage: Arbitrary[event1.employer.CompanyDetailsPage] =
    Arbitrary(event1.employer.CompanyDetailsPage(Index(0)))


  implicit lazy val arbitraryBenefitInKindBriefDescriptionPage: Arbitrary[BenefitInKindBriefDescriptionPage] =
    Arbitrary(member.BenefitInKindBriefDescriptionPage(Index(0)))

  implicit lazy val arbitrarySchemeUnAuthPaySurchargeMemberPage: Arbitrary[event1.SchemeUnAuthPaySurchargeMemberPage] =
    Arbitrary(event1.SchemeUnAuthPaySurchargeMemberPage(Index(0)))

  implicit lazy val arbitraryValueOfUnauthorisedPaymentPage: Arbitrary[event1.ValueOfUnauthorisedPaymentPage] =
    Arbitrary(event1.ValueOfUnauthorisedPaymentPage(Index(0)))

  implicit lazy val arbitraryDoYouHoldSignedMandatePage: Arbitrary[event1.DoYouHoldSignedMandatePage] =
    Arbitrary(event1.DoYouHoldSignedMandatePage(Index(0)))

  implicit lazy val arbitraryMembersDetailsPage: Arbitrary[MembersDetailsPage] =
    Arbitrary(MembersDetailsPage(Event1, 0))

  implicit lazy val arbitraryWhoReceivedUnauthPaymentPage: Arbitrary[event1.WhoReceivedUnauthPaymentPage] =
    Arbitrary(event1.WhoReceivedUnauthPaymentPage(Index(0)))

  implicit lazy val arbitraryPaymentNaturePage: Arbitrary[PaymentNaturePage] =
    Arbitrary(member.PaymentNaturePage(Index(0)))

  implicit lazy val arbitrarySchemeWindUpDatePage: Arbitrary[SchemeWindUpDatePage.type] =
    Arbitrary(SchemeWindUpDatePage)

  implicit lazy val arbitraryEvent18ConfirmationPage: Arbitrary[event18.Event18ConfirmationPage.type] =
    Arbitrary(event18.Event18ConfirmationPage)

  implicit lazy val arbitraryEventSummaryPage: Arbitrary[EventSummaryPage.type] =
    Arbitrary(EventSummaryPage)

  implicit lazy val arbitraryeventSelectionPage: Arbitrary[EventSelectionPage.type] =
    Arbitrary(EventSelectionPage)
}
