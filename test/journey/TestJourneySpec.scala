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

package journey

import data.SampleData.{companyDetails, seqAddresses, seqTolerantAddresses}
import generators.ModelGenerators
import models.EventSelection._
import models.common.{ChooseTaxYear, MembersDetails}
import models.enumeration.AddressJourneyType.{Event1EmployerAddressJourney, Event1EmployerPropertyAddressJourney, Event1MemberPropertyAddressJourney}
import models.enumeration.EventType
import models.event1.HowAddUnauthPayment.Manual
import models.event1.PaymentDetails
import models.event1.PaymentNature._
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.employer.LoanDetails
import models.event1.employer.PaymentNature.{LoansExceeding50PercentOfFundValue, ResidentialProperty, TangibleMoveableProperty}
import models.event1.member.ReasonForTheOverpaymentOrWriteOff.DeathOfMember
import models.event1.member.RefundOfContributions.WidowOrOrphan
import models.event1.member.WhoWasTheTransferMade.AnEmployerFinanced
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.EventSelectionPage
import pages.address.{ChooseAddressPage, EnterPostcodePage}
import pages.common.{ChooseTaxYearPage, MembersDetailsPage, TotalPensionAmountsPage}
import pages.event1._
import pages.event1.employer.{CompanyDetailsPage, EmployerTangibleMoveablePropertyPage, LoanDetailsPage}
import pages.event1.member._
import pages.event18.Event18ConfirmationPage
import pages.event22.HowAddAnnualAllowancePage
import pages.event18.Event18CheckYourAnswersPage
import pages.event23.HowAddDualAllowancePage
import pages.eventWindUp.{EventWindUpCheckYourAnswersPage, SchemeWindUpDatePage}

import java.time.LocalDate

class TestJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "test journey" in {

    startingFrom(Event18ConfirmationPage)
      .run(
        submitAnswer(Event18ConfirmationPage, true),
        pageMustBe(Event18CheckYourAnswersPage)
      )
  }
  "test windUp journey" in {

    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, EventWoundUp),
        submitAnswer(SchemeWindUpDatePage, LocalDate.of(2021, 5, 4)),
        pageMustBe(EventWindUpCheckYourAnswersPage)
      )
  }

  "test event1 journey (member), payment nature page" in {

    val membersDetails = arbitrary[MembersDetails].sample

    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, Event1),
        submitAnswer(HowAddUnauthPaymentPage(0), Manual),
        submitAnswer(WhoReceivedUnauthPaymentPage(0), Member),
        next,
        submitAnswer(MembersDetailsPage(EventType.Event1, 0), membersDetails.get),
        submitAnswer(DoYouHoldSignedMandatePage(0), true),
        submitAnswer(ValueOfUnauthorisedPaymentPage(0), true),
        submitAnswer(SchemeUnAuthPaySurchargeMemberPage(0), true),
        pageMustBe(member.PaymentNaturePage(0))
      )
  }

  "test event1 journey for Do You Hold Signed Mandate is false" in {
    startingFrom(DoYouHoldSignedMandatePage(0))
      .run(
        submitAnswer(DoYouHoldSignedMandatePage(0), false),
        submitAnswer(ValueOfUnauthorisedPaymentPage(0), true),
        submitAnswer(SchemeUnAuthPaySurchargeMemberPage(0), true),
        pageMustBe(member.PaymentNaturePage(0))
      )
  }

  "test event1 journey for unauthorised payment less than 25%" in {
    startingFrom(ValueOfUnauthorisedPaymentPage(0))
      .run(
        submitAnswer(ValueOfUnauthorisedPaymentPage(0), false),
        pageMustBe(member.PaymentNaturePage(0))
      )
  }

  "test event1 member-journey when Scheme Unauthorized Payment Surcharge false" in {
    startingFrom(ValueOfUnauthorisedPaymentPage(0))
      .run(
        submitAnswer(ValueOfUnauthorisedPaymentPage(0), true),
        submitAnswer(SchemeUnAuthPaySurchargeMemberPage(0), false),
        pageMustBe(member.PaymentNaturePage(0))
      )
  }

  "test event1 member-journey for error calc tax free lump sum" in {
    startingFrom(member.PaymentNaturePage(0))
      .run(
        submitAnswer(member.PaymentNaturePage(0), ErrorCalcTaxFreeLumpSums),
        submitAnswer(ErrorDescriptionPage(0), ""),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )

    startingFrom(member.PaymentNaturePage(0))
      .run(
        submitAnswer(member.PaymentNaturePage(0), ErrorCalcTaxFreeLumpSums),
        submitAnswer(ErrorDescriptionPage(0), "valid - description"),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )
  }

  "test event1 member-journey for benefits paid early" in {
    startingFrom(member.PaymentNaturePage(0))
      .run(
        submitAnswer(member.PaymentNaturePage(0), BenefitsPaidEarly),
        submitAnswer(BenefitsPaidEarlyPage(0), ""),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )

    startingFrom(member.PaymentNaturePage(0))
      .run(
        submitAnswer(member.PaymentNaturePage(0), BenefitsPaidEarly),
        submitAnswer(BenefitsPaidEarlyPage(0), "valid - description"),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )
  }

  "test event1 member-journey for benefit in kind - empty and description" in {
    startingFrom(member.PaymentNaturePage(0))
      .run(
        submitAnswer(member.PaymentNaturePage(0), BenefitInKind),
        submitAnswer(BenefitInKindBriefDescriptionPage(0), ""),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )

    startingFrom(member.PaymentNaturePage(0))
      .run(
        submitAnswer(member.PaymentNaturePage(0), BenefitInKind),
        submitAnswer(BenefitInKindBriefDescriptionPage(0), "valid - description"),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )
  }

  "test event1 journey (member), payment nature is reason for the overpayment/writeOff" in {
    startingFrom(member.PaymentNaturePage(0))
      .run(
        submitAnswer(member.PaymentNaturePage(0), OverpaymentOrWriteOff),
        submitAnswer(ReasonForTheOverpaymentOrWriteOffPage(0), DeathOfMember),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )
  }

  "test event1 journey (employer)" in {
    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, Event1),
        submitAnswer(HowAddUnauthPaymentPage(0), Manual),
        submitAnswer(WhoReceivedUnauthPaymentPage(0), Employer),
        pageMustBe(employer.WhatYouWillNeedPage(0))
      )

    startingFrom(CompanyDetailsPage(0))
      .run(
        submitAnswer(CompanyDetailsPage(0), companyDetails),
        submitAnswer(EnterPostcodePage(Event1EmployerAddressJourney, 0), seqTolerantAddresses),
        submitAnswer(ChooseAddressPage(Event1EmployerAddressJourney, 0), seqAddresses.head),
        pageMustBe(employer.PaymentNaturePage(0))
      )
  }

  "test nav to event1 residential property pages (member & employer)" in {
    startingFrom(PaymentNaturePage(0))
      .run(
        submitAnswer(PaymentNaturePage(0), ResidentialPropertyHeld),
        pageMustBe(pages.address.EnterPostcodePage(Event1MemberPropertyAddressJourney, 0))
      )

    startingFrom(pages.event1.employer.PaymentNaturePage(0))
      .run(
        submitAnswer(pages.event1.employer.PaymentNaturePage(0), ResidentialProperty),
        pageMustBe(pages.address.EnterPostcodePage(Event1EmployerPropertyAddressJourney, 0))
      )
  }

  "testing nav to event1 Refund of Contributions pages (member) and selecting Widow and/or Orphan option before continuing" in {
    startingFrom(PaymentNaturePage(0))
      .run(
        submitAnswer(PaymentNaturePage(0), RefundOfContributions),
        submitAnswer(RefundOfContributionsPage(0), WidowOrOrphan),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )
  }

  "testing nav to event1 loan details page" in {
    startingFrom(pages.event1.employer.PaymentNaturePage(0))
      .run(
        submitAnswer(pages.event1.employer.PaymentNaturePage(0), LoansExceeding50PercentOfFundValue),
        pageMustBe(LoanDetailsPage(0))
      )
  }

  "testing nav to event1 Refund of Contributions pages (member) and selecting EmployerOther option" in {
    startingFrom(PaymentNaturePage(0))
      .run(
        submitAnswer(PaymentNaturePage(0), RefundOfContributions),
        submitAnswer(RefundOfContributionsPage(0), models.event1.member.RefundOfContributions.Other),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )
  }

  "test nav to event1 Unauthorised Payment Recipient Name page(member) and Selecting Court order payment/confiscation order" in {
    startingFrom(PaymentNaturePage(0))
      .run(
        submitAnswer(PaymentNaturePage(0), CourtOrConfiscationOrder),
        pageMustBe(UnauthorisedPaymentRecipientNamePage(0))
      )
  }

  "test navigation to event1 tangible moveable property and payment nature description pages for member" in {
    startingFrom(member.PaymentNaturePage(0))
      .run(
        submitAnswer(PaymentNaturePage(0), TangibleMoveablePropertyHeld),
        pageMustBe(pages.event1.member.MemberTangibleMoveablePropertyPage(0))
      )

    startingFrom(member.PaymentNaturePage(0))
      .run(
        submitAnswer(PaymentNaturePage(0), MemberOther),
        pageMustBe(pages.event1.member.MemberPaymentNatureDescriptionPage(0))
      )
  }

  "test navigation to event1 tangible moveable property and payment nature description pages for employer" in {
    startingFrom(pages.event1.employer.PaymentNaturePage(0))
      .run(
        submitAnswer(pages.event1.employer.PaymentNaturePage(0), TangibleMoveableProperty),
        pageMustBe(pages.event1.employer.EmployerTangibleMoveablePropertyPage(0))
      )

    startingFrom(pages.event1.employer.PaymentNaturePage(0))
      .run(
        submitAnswer(pages.event1.employer.PaymentNaturePage(0), models.event1.employer.PaymentNature.EmployerOther),
        pageMustBe(pages.event1.employer.EmployerPaymentNatureDescriptionPage(0))
      )
  }

  "test navigation to event1 Transfer to non-registered pension scheme journey for member" in {
    startingFrom(member.PaymentNaturePage(0))
      .run(
        submitAnswer(PaymentNaturePage(0), TransferToNonRegPensionScheme),
        submitAnswer(WhoWasTheTransferMadePage(0), AnEmployerFinanced),
        pageMustBe(pages.event1.member.SchemeDetailsPage(0))
      )
  }

  "test navigation to event22 from event selection page to totalAmounts page" in {
    val membersDetails = arbitrary[MembersDetails].sample
    val taxYear = arbitrary[ChooseTaxYear].sample
    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, Event22),
        submitAnswer(HowAddAnnualAllowancePage(0), models.event22.HowAddAnnualAllowance.Manual),
        next,
        submitAnswer(pages.common.MembersDetailsPage(EventType.Event22, 0), membersDetails.get),
        submitAnswer(ChooseTaxYearPage(EventType.Event22, 0), taxYear.get),
        pageMustBe(TotalPensionAmountsPage(EventType.Event22, 0))
      )
  }

  "test navigation to event23 from event selection page to totalAmounts page" in {
    val membersDetails = arbitrary[MembersDetails].sample
    val taxYear = arbitrary[ChooseTaxYear].sample
    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, Event23),
        submitAnswer(HowAddDualAllowancePage(0), models.event23.HowAddDualAllowance.Manual),
        next,
        submitAnswer(pages.common.MembersDetailsPage(EventType.Event23, 0), membersDetails.get),
        submitAnswer(ChooseTaxYearPage(EventType.Event23, 0), taxYear.get),
        pageMustBe(TotalPensionAmountsPage(EventType.Event23, 0))
      )
  }

  "testing nav to CYA page after changing payment nature from Benefit in kind to refund of contributions option" in {
    startingFrom(PaymentNaturePage(0))
      .run(
        submitAnswer(PaymentNaturePage(0), BenefitInKind),
        submitAnswer(BenefitInKindBriefDescriptionPage(0), "valid - description"),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        goToChangeAnswer(PaymentNaturePage(0)),
        submitAnswer(PaymentNaturePage(0), RefundOfContributions),
        submitAnswer(RefundOfContributionsPage(0), models.event1.member.RefundOfContributions.Other),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )
  }

  "testing nav to CYA page after changing payment nature from loans exceeding 50 percent of fund value to tangible moveable property option for employer" in {
    startingFrom(employer.PaymentNaturePage(0))
      .run(
        submitAnswer(employer.PaymentNaturePage(0), LoansExceeding50PercentOfFundValue),
        submitAnswer(LoanDetailsPage(0), LoanDetails(Some(1000.00), Some(2000.22))),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(1000.00, LocalDate.now())),
        goToChangeAnswer(employer.PaymentNaturePage(0)),
        submitAnswer(employer.PaymentNaturePage(0), TangibleMoveableProperty),
        submitAnswer(EmployerTangibleMoveablePropertyPage(0), "tangible moveable"),
        submitAnswer(PaymentValueAndDatePage(0), PaymentDetails(3000.00, LocalDate.now())),
        comparePageMustBeAsString(Event1CheckYourAnswersPage(0))
      )
  }
  "testing nav from summary page to sanctions page in event 1" in {
    startingFrom(UnauthPaymentSummaryPage)
      .run(
        submitAnswer(UnauthPaymentSummaryPage, false),
        pageMustBe(UnauthPaymentAndSanctionChargesPage)
      )
  }

}
