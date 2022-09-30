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
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import models.event1.HowAddUnauthPayment.Manual
import models.event1.MembersDetails
import models.event1.PaymentNature.ErrorCalcTaxFreeLumpSums
import models.event1.PaymentNature.BenefitInKind
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.address.{ChooseAddressPage, EnterPostcodePage}
import pages.event1._
import pages.event1.employer.CompanyDetailsPage
import pages.event1.member.ErrorDescriptionPage
import pages.event1.{WhatYouWillNeedPage, _}
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

  "test event1 member-journey for unauthorised payment more than 25% when Scheme Unauthorized Payment Surcharge true" in {

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

  "test event1 member-journey for unauthorised payment more than 25% when Scheme Unauthorized Payment Surcharge false" in {

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
        submitAnswer(SchemeUnAuthPaySurchargeMemberPage, false),
        pageMustBe(pages.event1.PaymentNaturePage)
      )
  }

  "test event1 member-journey for error description" in {

    val membersDetails = arbitrary[MembersDetails].sample

    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, Event1),
        submitAnswer(HowAddUnauthPaymentPage, Manual),
        submitAnswer(WhoReceivedUnauthPaymentPage, Member),
        next,
        submitAnswer(MembersDetailsPage, membersDetails.get),
        submitAnswer(DoYouHoldSignedMandatePage, true),
        submitAnswer(ValueOfUnauthorisedPaymentPage, false),
        submitAnswer(pages.event1.PaymentNaturePage, ErrorCalcTaxFreeLumpSums),
        pageMustBe(ErrorDescriptionPage)
      )
  }


  "test event1 member-journey for unauthorised payment less than 25%" in {

    val membersDetails = arbitrary[MembersDetails].sample

    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, Event1),
        submitAnswer(HowAddUnauthPaymentPage, Manual),
        submitAnswer(WhoReceivedUnauthPaymentPage, Member),
        next,
        submitAnswer(MembersDetailsPage, membersDetails.get),
        submitAnswer(DoYouHoldSignedMandatePage, true),
        submitAnswer(ValueOfUnauthorisedPaymentPage, false),
        pageMustBe(pages.event1.PaymentNaturePage)
      )
  }

  "test event1 member-journey for benefit in kind - empty" in {
    val membersDetails = arbitrary[MembersDetails].sample

    val paymentNatureJourney =
      journeyOf(
        submitAnswer(EventSelectionPage, Event1),
        submitAnswer(HowAddUnauthPaymentPage, Manual),
        submitAnswer(WhoReceivedUnauthPaymentPage, Member),
        next,
        submitAnswer(MembersDetailsPage, membersDetails.get),
        submitAnswer(DoYouHoldSignedMandatePage, true),
        submitAnswer(ValueOfUnauthorisedPaymentPage, false),
      )

    startingFrom(EventSelectionPage)
      .run(
        paymentNatureJourney,
        submitAnswer(pages.event1.PaymentNaturePage, BenefitInKind),
        submitAnswer(BenefitInKindBriefDescriptionPage, ""),
        pageMustBe(IndexPage)
      )
  }

  "test event1 member-journey for benefit in kind - with some description" in {
    val membersDetails = arbitrary[MembersDetails].sample

    val paymentNatureJourney =
      journeyOf(
        submitAnswer(EventSelectionPage, Event1),
        submitAnswer(HowAddUnauthPaymentPage, Manual),
        submitAnswer(WhoReceivedUnauthPaymentPage, Member),
        pageMustBe(WhatYouWillNeedPage),
        next,
        submitAnswer(MembersDetailsPage, membersDetails.get),
        submitAnswer(DoYouHoldSignedMandatePage, true),
        submitAnswer(ValueOfUnauthorisedPaymentPage, false),
      )

    startingFrom(EventSelectionPage)
      .run(
        paymentNatureJourney,
        submitAnswer(pages.event1.PaymentNaturePage, BenefitInKind),
        submitAnswer(BenefitInKindBriefDescriptionPage, "valid - description"),
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
}
