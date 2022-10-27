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
import models.common.MembersDetails
import models.enumeration.AddressJourneyType.{Event1EmployerAddressJourney, Event1EmployerPropertyAddressJourney, Event1MemberPropertyAddressJourney}
import models.enumeration.EventType
import models.event1.HowAddUnauthPayment.Manual
import models.event1.PaymentDetails
import models.event1.PaymentNature.{BenefitInKind, BenefitsPaidEarly, CourtOrConfiscationOrder, ErrorCalcTaxFreeLumpSums, Other, OverpaymentOrWriteOff, RefundOfContributions, ResidentialPropertyHeld, TangibleMoveablePropertyHeld, TransferToNonRegPensionScheme}
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.employer.LoanDetails
import models.event1.employer.PaymentNature.{LoansExceeding50PercentOfFundValue, ResidentialProperty, TangibleMoveableProperty}
import models.event1.member.ReasonForTheOverpaymentOrWriteOff.DeathOfMember
import models.event1.member.RefundOfContributions.WidowOrOrphan
import models.event1.member.WhoWasTheTransferMade.AnEmployerFinanced
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.address.{ChooseAddressPage, EnterPostcodePage}
import pages.common.MembersDetailsPage
import pages.event1._
import pages.event1.employer.{CompanyDetailsPage, EmployerTangibleMoveablePropertyPage, LoanDetailsPage}
import pages.event1.member._
import pages.event18.Event18ConfirmationPage
import pages.event23.HowAddDualAllowancePage
import pages.eventWindUp.SchemeWindUpDatePage
import pages.{CheckYourAnswersPage, EventSelectionPage}

import java.time.LocalDate

class TestJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "test journey" in {

    startingFrom(Event18ConfirmationPage)
      .run(
        submitAnswer(Event18ConfirmationPage, true),
        pageMustBe(CheckYourAnswersPage.event18)
      )
  }
  "test windUp journey" in {

    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, EventWoundUp),
        submitAnswer(SchemeWindUpDatePage, LocalDate.of(2021, 5, 4)),
        pageMustBe(pages.CheckYourAnswersPage.windUp)
      )
  }

  "test event1 journey (member), payment nature page" in {

    val membersDetails = arbitrary[MembersDetails].sample

    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, Event1),
        submitAnswer(HowAddUnauthPaymentPage, Manual),
        submitAnswer(WhoReceivedUnauthPaymentPage, Member),
        next,
        submitAnswer(MembersDetailsPage(EventType.Event1), membersDetails.get),
        submitAnswer(DoYouHoldSignedMandatePage, true),
        submitAnswer(ValueOfUnauthorisedPaymentPage, true),
        submitAnswer(SchemeUnAuthPaySurchargeMemberPage, true),
        pageMustBe(member.PaymentNaturePage)
      )
  }

  "test event1 journey for Do You Hold Signed Mandate is false" in {
    startingFrom(DoYouHoldSignedMandatePage)
      .run(
        submitAnswer(DoYouHoldSignedMandatePage, false),
        submitAnswer(ValueOfUnauthorisedPaymentPage, true),
        submitAnswer(SchemeUnAuthPaySurchargeMemberPage, true),
        pageMustBe(member.PaymentNaturePage)
      )
  }

  "test event1 journey for unauthorised payment less than 25%" in {
    startingFrom(ValueOfUnauthorisedPaymentPage)
      .run(
        submitAnswer(ValueOfUnauthorisedPaymentPage, false),
        pageMustBe(member.PaymentNaturePage)
      )
  }

  "test event1 member-journey when Scheme Unauthorized Payment Surcharge false" in {
    startingFrom(ValueOfUnauthorisedPaymentPage)
      .run(
        submitAnswer(ValueOfUnauthorisedPaymentPage, true),
        submitAnswer(SchemeUnAuthPaySurchargeMemberPage, false),
        pageMustBe(member.PaymentNaturePage)
      )
  }

  "test event1 member-journey for error calc tax free lump sum" in {
    startingFrom(member.PaymentNaturePage)
      .run(
        submitAnswer(member.PaymentNaturePage, ErrorCalcTaxFreeLumpSums),
        submitAnswer(ErrorDescriptionPage, ""),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )

    startingFrom(member.PaymentNaturePage)
      .run(
        submitAnswer(member.PaymentNaturePage, ErrorCalcTaxFreeLumpSums),
        submitAnswer(ErrorDescriptionPage, "valid - description"),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )
  }

  "test event1 member-journey for benefits paid early" in {
    startingFrom(member.PaymentNaturePage)
      .run(
        submitAnswer(member.PaymentNaturePage, BenefitsPaidEarly),
        submitAnswer(BenefitsPaidEarlyPage, ""),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )

    startingFrom(member.PaymentNaturePage)
      .run(
        submitAnswer(member.PaymentNaturePage, BenefitsPaidEarly),
        submitAnswer(BenefitsPaidEarlyPage, "valid - description"),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )
  }

  "test event1 member-journey for benefit in kind - empty and description" in {
    startingFrom(member.PaymentNaturePage)
      .run(
        submitAnswer(member.PaymentNaturePage, BenefitInKind),
        submitAnswer(BenefitInKindBriefDescriptionPage, ""),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )

    startingFrom(member.PaymentNaturePage)
      .run(
        submitAnswer(member.PaymentNaturePage, BenefitInKind),
        submitAnswer(BenefitInKindBriefDescriptionPage, "valid - description"),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )
  }

  "test event1 journey (member), payment nature is reason for the overpayment/writeOff" in {
    startingFrom(member.PaymentNaturePage)
      .run(
        submitAnswer(member.PaymentNaturePage, OverpaymentOrWriteOff),
        submitAnswer(ReasonForTheOverpaymentOrWriteOffPage, DeathOfMember),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )
  }

  "test event1 journey (employer)" in {
    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, Event1),
        submitAnswer(HowAddUnauthPaymentPage, Manual),
        submitAnswer(WhoReceivedUnauthPaymentPage, Employer),
        pageMustBe(employer.WhatYouWillNeedPage)
      )

    startingFrom(CompanyDetailsPage)
      .run(
        submitAnswer(CompanyDetailsPage, companyDetails),
        submitAnswer(EnterPostcodePage(Event1EmployerAddressJourney), seqTolerantAddresses),
        submitAnswer(ChooseAddressPage(Event1EmployerAddressJourney), seqAddresses.head),
        pageMustBe(employer.PaymentNaturePage)
      )
  }

  "test nav to event1 residential property pages (member & employer)" in {
    startingFrom(PaymentNaturePage)
      .run(
        submitAnswer(PaymentNaturePage, ResidentialPropertyHeld),
        pageMustBe(pages.address.EnterPostcodePage(Event1MemberPropertyAddressJourney))
      )

    startingFrom(pages.event1.employer.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.employer.PaymentNaturePage, ResidentialProperty),
        pageMustBe(pages.address.EnterPostcodePage(Event1EmployerPropertyAddressJourney))
      )
  }

  "testing nav to event1 Refund of Contributions pages (member) and selecting Widow and/or Orphan option before continuing" in {
    startingFrom(PaymentNaturePage)
      .run(
        submitAnswer(PaymentNaturePage, RefundOfContributions),
        submitAnswer(RefundOfContributionsPage, WidowOrOrphan),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )
  }

  "testing nav to event1 loan details page" in {
    startingFrom(pages.event1.employer.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.employer.PaymentNaturePage, LoansExceeding50PercentOfFundValue),
        pageMustBe(LoanDetailsPage)
      )
  }

  "testing nav to event1 Refund of Contributions pages (member) and selecting Other option" in {
    startingFrom(PaymentNaturePage)
      .run(
        submitAnswer(PaymentNaturePage, RefundOfContributions),
        submitAnswer(RefundOfContributionsPage, models.event1.member.RefundOfContributions.Other),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )
  }

  "test nav to event1 Unauthorised Payment Recipient Name page(member) and Selecting Court order payment/confiscation order" in {
    startingFrom(PaymentNaturePage)
      .run(
        submitAnswer(PaymentNaturePage, CourtOrConfiscationOrder),
        pageMustBe(UnauthorisedPaymentRecipientNamePage)
      )
  }

  "test navigation to event1 tangible moveable property and payment nature description pages for member" in {
    startingFrom(member.PaymentNaturePage)
      .run(
        submitAnswer(PaymentNaturePage, TangibleMoveablePropertyHeld),
        pageMustBe(pages.event1.member.MemberTangibleMoveablePropertyPage)
      )

    startingFrom(member.PaymentNaturePage)
      .run(
        submitAnswer(PaymentNaturePage, Other),
        pageMustBe(pages.event1.member.MemberPaymentNatureDescriptionPage)
      )
  }

  "test navigation to event1 tangible moveable property and payment nature description pages for employer" in {
    startingFrom(pages.event1.employer.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.employer.PaymentNaturePage, TangibleMoveableProperty),
        pageMustBe(pages.event1.employer.EmployerTangibleMoveablePropertyPage)
      )

    startingFrom(pages.event1.employer.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.employer.PaymentNaturePage, models.event1.employer.PaymentNature.Other),
        pageMustBe(pages.event1.employer.EmployerPaymentNatureDescriptionPage)
      )
  }

  "test navigation to event1 Transfer to non-registered pension scheme journey for member" in {
    startingFrom(member.PaymentNaturePage)
      .run(
        submitAnswer(PaymentNaturePage, TransferToNonRegPensionScheme),
        submitAnswer(WhoWasTheTransferMadePage, AnEmployerFinanced),
        pageMustBe(pages.event1.member.SchemeDetailsPage)
      )
  }

  "test navigation to event23 from event selection page to membersDetails" in {
    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, Event23),
        submitAnswer(HowAddDualAllowancePage, models.event23.HowAddDualAllowance.Manual),
        next,
        pageMustBe(pages.common.MembersDetailsPage(EventType.Event23))
      )
  }

  "testing nav to CYA page after changing payment nature from Benefit in kind to refund of contributions option" in {
    startingFrom(PaymentNaturePage)
      .run(
        submitAnswer(PaymentNaturePage, BenefitInKind),
        submitAnswer(BenefitInKindBriefDescriptionPage, "valid - description"),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        goToChangeAnswer(PaymentNaturePage),
        submitAnswer(PaymentNaturePage, RefundOfContributions),
        submitAnswer(RefundOfContributionsPage, models.event1.member.RefundOfContributions.Other),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )
  }

  "testing nav to CYA page after changing payment nature from loans exceeding 50 percent of fund value to tangible moveable property option for employer" in {
    startingFrom(employer.PaymentNaturePage)
      .run(
        submitAnswer(employer.PaymentNaturePage, LoansExceeding50PercentOfFundValue),
        submitAnswer(LoanDetailsPage, LoanDetails(Some(1000.00), Some(2000.22))),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(1000.00, LocalDate.now())),
        goToChangeAnswer(employer.PaymentNaturePage),
        submitAnswer(employer.PaymentNaturePage, TangibleMoveableProperty),
        submitAnswer(EmployerTangibleMoveablePropertyPage, "tangible moveable"),
        submitAnswer(PaymentValueAndDatePage, PaymentDetails(3000.00, LocalDate.now())),
        comparePageMustBeAsString(CheckYourAnswersPage(EventType.Event1))
      )
  }
}
