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
import models.TestCheckBox.writes
import models.enumeration.AddressJourneyType.{Event1EmployerAddressJourney, Event1EmployerPropertyAddressJourney, Event1MemberPropertyAddressJourney}
import models.event1.HowAddUnauthPayment.Manual
import models.event1.MembersDetails
import models.event1.PaymentNature.{BenefitInKind, BenefitsPaidEarly, CourtOrConfiscationOrder, ErrorCalcTaxFreeLumpSums, OverpaymentOrWriteOff, RefundOfContributions, ResidentialPropertyHeld}
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.employer.PaymentNature.ResidentialProperty
import models.event1.member.ReasonForTheOverpaymentOrWriteOff.DeathOfMember
import models.event1.member.RefundOfContributions.{Other, WidowOrOrphan}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.address.{ChooseAddressPage, EnterPostcodePage}
import pages.event1._
import pages.event1.employer.CompanyDetailsPage
import pages.event1.member.{BenefitsPaidEarlyPage, ErrorDescriptionPage, ReasonForTheOverpaymentOrWriteOffPage, RefundOfContributionsPage, UnauthorisedPaymentRecipientNamePage}
import pages.event18.Event18ConfirmationPage
import pages.eventWindUp.SchemeWindUpDatePage
import pages.{CheckYourAnswersPage, EventSelectionPage, IndexPage}

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
        submitAnswer(MembersDetailsPage, membersDetails.get),
        submitAnswer(DoYouHoldSignedMandatePage, true),
        submitAnswer(ValueOfUnauthorisedPaymentPage, true),
        submitAnswer(SchemeUnAuthPaySurchargeMemberPage, true),
        pageMustBe(pages.event1.PaymentNaturePage)
      )
  }

  "test event1 journey for Do You Hold Signed Mandate is false" in {
    startingFrom(DoYouHoldSignedMandatePage)
      .run(
        submitAnswer(DoYouHoldSignedMandatePage, false),
        submitAnswer(ValueOfUnauthorisedPaymentPage, true),
        submitAnswer(SchemeUnAuthPaySurchargeMemberPage, true),
        pageMustBe(pages.event1.PaymentNaturePage)
      )
  }

  "test event1 journey for unauthorised payment less than 25%" in {
    startingFrom(ValueOfUnauthorisedPaymentPage)
      .run(
        submitAnswer(ValueOfUnauthorisedPaymentPage, false),
        pageMustBe(pages.event1.PaymentNaturePage)
      )
  }

  "test event1 member-journey when Scheme Unauthorized Payment Surcharge false" in {
    startingFrom(ValueOfUnauthorisedPaymentPage)
      .run(
        submitAnswer(ValueOfUnauthorisedPaymentPage, true),
        submitAnswer(SchemeUnAuthPaySurchargeMemberPage, false),
        pageMustBe(pages.event1.PaymentNaturePage)
      )
  }

  "test event1 member-journey for error calc tax free lump sum" in {
    startingFrom(pages.event1.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.PaymentNaturePage, ErrorCalcTaxFreeLumpSums),
        submitAnswer(ErrorDescriptionPage, ""),
        pageMustBe(IndexPage)
      )

    startingFrom(pages.event1.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.PaymentNaturePage, ErrorCalcTaxFreeLumpSums),
        submitAnswer(ErrorDescriptionPage, "valid - description"),
        pageMustBe(IndexPage)
      )
  }

  "test event1 member-journey for benefits paid early" in {
    startingFrom(pages.event1.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.PaymentNaturePage, BenefitsPaidEarly),
        submitAnswer(BenefitsPaidEarlyPage, ""),
        pageMustBe(IndexPage)
      )

    startingFrom(pages.event1.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.PaymentNaturePage, BenefitsPaidEarly),
        submitAnswer(BenefitsPaidEarlyPage, "valid - description"),
        pageMustBe(IndexPage)
      )
  }

  "test event1 member-journey for benefit in kind - empty and description" in {
    startingFrom(pages.event1.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.PaymentNaturePage, BenefitInKind),
        submitAnswer(BenefitInKindBriefDescriptionPage, ""),
        pageMustBe(IndexPage)
      )

    startingFrom(pages.event1.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.PaymentNaturePage, BenefitInKind),
        submitAnswer(BenefitInKindBriefDescriptionPage, "valid - description"),
        pageMustBe(IndexPage)
      )
  }

  "test event1 journey (member), payment nature is reason for the overpayment/writeOff" in {
    startingFrom(pages.event1.PaymentNaturePage)
      .run(
        submitAnswer(pages.event1.PaymentNaturePage, OverpaymentOrWriteOff),
        submitAnswer(ReasonForTheOverpaymentOrWriteOffPage, DeathOfMember),
        pageMustBe(IndexPage)
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
        pageMustBe(IndexPage)
      )
  }
  "testing nav to event1 Refund of Contributions pages (member) and selecting Other option" in {
    startingFrom(PaymentNaturePage)
      .run(
        submitAnswer(PaymentNaturePage, RefundOfContributions),
        submitAnswer(RefundOfContributionsPage, Other),
        pageMustBe(IndexPage)
      )
  }
  "test nav to event1 Unauthorised Payment Recipient Name page(member) and Selecting Court order payment/confiscation order" in {
    startingFrom(PaymentNaturePage)
      .run(
        submitAnswer(PaymentNaturePage, CourtOrConfiscationOrder),
        pageMustBe(UnauthorisedPaymentRecipientNamePage)
      )
  }
}
