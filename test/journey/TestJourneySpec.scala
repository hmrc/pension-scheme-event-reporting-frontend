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

package journey

import data.SampleData.{companyDetails, seqTolerantAddresses}
import generators.ModelGenerators
import models.{EventSelection, TaxYear, UserAnswers}
import models.EventSelection._
import models.common.ManualOrUpload.Manual
import models.common.{ChooseTaxYear, MembersDetails}
import models.enumeration.AddressJourneyType.{Event1EmployerAddressJourney, Event1EmployerPropertyAddressJourney, Event1MemberPropertyAddressJourney}
import models.enumeration.{EventType, JourneyStartType}
import models.event1.PaymentDetails
import models.event1.PaymentNature._
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.employer.LoanDetails
import models.event1.employer.PaymentNature.{LoansExceeding50PercentOfFundValue, ResidentialProperty, TangibleMoveableProperty}
import models.event1.member.ReasonForTheOverpaymentOrWriteOff.DeathOfMember
import models.event1.member.RefundOfContributions.WidowOrOrphan
import models.event1.member.WhoWasTheTransferMade.AnEmployerFinanced
import models.event10.{BecomeOrCeaseScheme, SchemeChangeDate}
import models.event12.DateOfChange
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.address.ManualAddressPage
import pages.common.{ChooseTaxYearPage, ManualOrUploadPage, MembersDetailsPage, TotalPensionAmountsPage}
import pages.event1._
import pages.event1.employer.{CompanyDetailsPage, EmployerTangibleMoveablePropertyPage, LoanDetailsPage}
import pages.event1.member._
import pages.event10.{BecomeOrCeaseSchemePage, ContractsOrPoliciesPage, Event10CheckYourAnswersPage, SchemeChangeDatePage}
import pages.event12.{CannotSubmitPage, DateOfChangePage, Event12CheckYourAnswersPage, HasSchemeChangedRulesPage}
import pages.event18.Event18ConfirmationPage
import pages.eventWindUp.{EventWindUpCheckYourAnswersPage, SchemeWindUpDatePage}
import pages.{DeclarationPage, EventReportingTileLinksPage, EventSelectionPage, EventSummaryPage, TaxYearPage, WantToSubmitPage}
import play.api.libs.json.Writes
//import pages.fileUpload.FileUploadResultPage
//import pages.fileUpload.ProcessingRequestPage

import java.time.{LocalDate, Month}

class TestJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {
  private val writesTaxYear: Writes[ChooseTaxYear]= ChooseTaxYear.writes(ChooseTaxYear.enumerable(2021))

  "test journey" in {

    startingFrom(Event18ConfirmationPage)
      .run(
        submitAnswer(Event18ConfirmationPage, true),
        pageMustBe(EventSummaryPage)
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
        submitAnswer(ManualOrUploadPage(EventType.Event1, 0), Manual),
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
        submitAnswer(ManualOrUploadPage(EventType.Event1, 0), Manual),
        submitAnswer(WhoReceivedUnauthPaymentPage(0), Employer),
        pageMustBe(employer.WhatYouWillNeedPage(0))
      )

    startingFrom(CompanyDetailsPage(0))
      .run(
        submitAnswer(CompanyDetailsPage(0), companyDetails),
        goTo(ManualAddressPage(Event1EmployerAddressJourney, 0)),
        submitAnswer(ManualAddressPage(Event1EmployerAddressJourney, 0), seqTolerantAddresses.head.toAddress.get),
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
        submitAnswer(ManualOrUploadPage(EventType.Event22, 0), Manual),
        next,
        submitAnswer(pages.common.MembersDetailsPage(EventType.Event22, 0), membersDetails.get),
        submitAnswer(ChooseTaxYearPage(EventType.Event22, 0), taxYear.get)(writesTaxYear, implicitly),
        pageMustBe(TotalPensionAmountsPage(EventType.Event22, 0))
      )
  }

  "test navigation to event23 from event selection page to totalAmounts page" in {
    val membersDetails = arbitrary[MembersDetails].sample
    val taxYear = arbitrary[ChooseTaxYear].sample
    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, Event23),
        submitAnswer(ManualOrUploadPage(EventType.Event23, 0), Manual),
        next,
        submitAnswer(pages.common.MembersDetailsPage(EventType.Event23, 0), membersDetails.get),
        submitAnswer(ChooseTaxYearPage(EventType.Event23, 0), taxYear.get)(writesTaxYear, implicitly),
        pageMustBe(TotalPensionAmountsPage(EventType.Event23, 0))
      )
  }

  "test navigation from tax year page to event summary page after user chosen compiled events" in {
    val ua = UserAnswers()
      .setOrException(EventReportingTileLinksPage, JourneyStartType.InProgress, nonEventTypeData = true)
    startingFrom(TaxYearPage, answers = ua)
      .run(
        submitAnswer(TaxYearPage, TaxYear("2020")),
        pageMustBe(EventSummaryPage)
      )
  }

  "test navigation from tax year page to event summary page after user chosen past submitted events" in {
    val ua = UserAnswers()
      .setOrException(EventReportingTileLinksPage, JourneyStartType.PastEventTypes, nonEventTypeData = true)
    startingFrom(TaxYearPage, answers = ua)
      .run(
        submitAnswer(TaxYearPage, TaxYear("2020")),
        pageMustBe(EventSummaryPage)
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

  "testing navigation from event summary to wantToSubmit page" in {
    startingFrom(EventSummaryPage)
      .run(
        submitAnswer(EventSummaryPage, false),
        pageMustBe(WantToSubmitPage)
      )
  }

  "testing navigation from want to submitPage to the declaration page" in {
    startingFrom(WantToSubmitPage)
      .run(
        submitAnswer(WantToSubmitPage, true),
        pageMustBe(DeclarationPage)
      )
  }

  "Event 10" - {
    "testing navigation from the Event Selection page to 'What change has taken place for this pension scheme?' page" in {
      startingFrom(EventSelectionPage)
        .run(
          submitAnswer(EventSelectionPage, EventSelection.Event10),
          pageMustBe(BecomeOrCeaseSchemePage)
        )
    }
    "testing navigation from 'What change has taken place for this pension scheme?' page to scheme date page (Became a scheme)" in {
      startingFrom(BecomeOrCeaseSchemePage)
        .run(
          submitAnswer(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItBecameAnInvestmentRegulatedPensionScheme),
          pageMustBe(SchemeChangeDatePage)
        )
    }
    "testing navigation from 'What change has taken place for this pension scheme?' page to scheme date page (Ceased to become a scheme)" in {
      startingFrom(BecomeOrCeaseSchemePage)
        .run(
          submitAnswer(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItHasCeasedToBeAnInvestmentRegulatedPensionScheme),
          pageMustBe(SchemeChangeDatePage)
        )
    }
    "testing navigation from 'When did the scheme become an investment regulated pension scheme?' page to ContractsOrPolicies page (Become a scheme)" in {
      startingFrom(BecomeOrCeaseSchemePage)
        .run(
          submitAnswer(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItBecameAnInvestmentRegulatedPensionScheme),
          submitAnswer(SchemeChangeDatePage, SchemeChangeDate(LocalDate.of(2022, Month.MAY, 22))),
          pageMustBe(ContractsOrPoliciesPage)
        )
    }
    "testing navigation from 'When did this scheme cease to be an investment regulated pension scheme?' page to CYA page (Ceased to become a scheme)" in {
      startingFrom(BecomeOrCeaseSchemePage)
        .run(
          submitAnswer(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItHasCeasedToBeAnInvestmentRegulatedPensionScheme),
          submitAnswer(SchemeChangeDatePage, SchemeChangeDate(LocalDate.of(2022, Month.MAY, 22))),
          pageMustBe(Event10CheckYourAnswersPage())
        )
    }
    "testing navigation from 'Do all investments consist of either contracts or policies of insurance?' page to CYA page (Become a scheme: Yes)" in {
      startingFrom(ContractsOrPoliciesPage)
        .run(
          submitAnswer(ContractsOrPoliciesPage, true),
          pageMustBe(Event10CheckYourAnswersPage())
        )
    }
    "testing navigation from 'Do all investments consist of either contracts or policies of insurance?' page to CYA page (Become a scheme: No)" in {
      startingFrom(ContractsOrPoliciesPage)
        .run(
          submitAnswer(ContractsOrPoliciesPage, false),
          pageMustBe(Event10CheckYourAnswersPage())
        )
    }
    "testing navigation from CYA page to Event Summary page" in {
      startingFrom(Event10CheckYourAnswersPage())
        .run(
          goTo(EventSummaryPage),
          pageMustBe(EventSummaryPage)
        )
    }
    "testing navigation from Event Summary page to CYA page" in {
      startingFrom(EventSummaryPage)
        .run(
          goTo(Event10CheckYourAnswersPage()),
          pageMustBe(Event10CheckYourAnswersPage())
        )
    }
  }

  "Event 12" - {
    "testing navigation from the Event Selection page to 'Has the scheme that was treated as 2 or more schemes immediately before 6 April 2006 changed its rules?' page" in {
      startingFrom(EventSelectionPage)
        .run(
          submitAnswer(EventSelectionPage, EventSelection.Event12),
          pageMustBe(HasSchemeChangedRulesPage)
        )
    }
    "testing navigation from 'Has the scheme that was treated as 2 or more schemes immediately before 6 April 2006 changed its rules?' page to date page (Yes)" in {
      startingFrom(HasSchemeChangedRulesPage)
        .run(
          submitAnswer(HasSchemeChangedRulesPage, true),
          pageMustBe(DateOfChangePage)
        )
    }
    "testing navigation from 'Has the scheme that was treated as 2 or more schemes immediately before 6 April 2006 changed its rules?' page to date page (No)" in {
      startingFrom(HasSchemeChangedRulesPage)
        .run(
          submitAnswer(HasSchemeChangedRulesPage, false),
          pageMustBe(CannotSubmitPage)
        )
    }
    "testing navigation from 'You cannot submit a report for this event' page to the Event Selection page" in {
      startingFrom(CannotSubmitPage)
        .run(
          goTo(EventSelectionPage),
          pageMustBe(EventSelectionPage)
        )
    }
    "testing navigation from 'When did this change take effect?' page to CYA page" in {
      startingFrom(DateOfChangePage)
        .run(
          submitAnswer(DateOfChangePage, DateOfChange(LocalDate.of(2022, Month.MAY, 22))),
          pageMustBe(Event12CheckYourAnswersPage())
        )
    }
    "testing navigation from CYA page to Event Summary page" in {
      startingFrom(Event12CheckYourAnswersPage())
        .run(
          goTo(EventSummaryPage),
          pageMustBe(EventSummaryPage)
        )
    }
    "testing navigation from Event Summary page to CYA page" in {
      startingFrom(EventSummaryPage)
        .run(
          goTo(Event12CheckYourAnswersPage()),
          pageMustBe(Event12CheckYourAnswersPage())
        )
    }
  }

}
