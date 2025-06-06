/*
 * Copyright 2024 HM Revenue & Customs
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

package data

import base.SpecBase
import models.address.{Address, TolerantAddress}
import models.common.ManualOrUpload.Manual
import models.common.{ChooseTaxYear, MembersDetails, PaymentDetails => CommonPaymentDetails}
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import models.enumeration.EventType.{Event1, Event2, Event6, Event7, Event8, Event8A}
import models.enumeration.VersionStatus.Compiled
import models.enumeration.{EventType, VersionStatus}
import models.event1.PaymentNature.BenefitInKind
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.employer.PaymentNature.TangibleMoveableProperty
import models.event1.employer.{CompanyDetails, LoanDetails}
import models.event1.member.SchemeDetails
import models.event1.{PaymentDetails => Event1PaymentDetails}
import models.event10.{BecomeOrCeaseScheme, SchemeChangeDate}
import models.event11.Event11Date
import models.event12.DateOfChange
import models.event13.SchemeStructure
import models.event20.Event20Date
import models.event20.WhatChange.BecameOccupationalScheme
import models.event20A.WhatChange.{BecameMasterTrust, CeasedMasterTrust}
import models.event24.{BCETypeSelection, CrystallisedDate, ProtectionReferenceData}
import models.event6.{CrystallisedDetails, TypeOfProtection => Event6TypeOfProtection}
import models.event7.PaymentDate
import models.event8.{LumpSumDetails, TypeOfProtection => Event8TypeOfProtection}
import models.event8a.PaymentType
import models.{EROverview, EROverviewVersion, TaxYear, UserAnswers, VersionInfo}
import pages.address.ManualAddressPage
import pages.common._
import pages.event1._
import pages.event1.employer.{CompanyDetailsPage, EmployerTangibleMoveablePropertyPage, PaymentNaturePage => EmployerPaymentNaturePage}
import pages.event1.member.{BenefitInKindBriefDescriptionPage, PaymentNaturePage => MemberPaymentNaturePage}
import pages.event10.{BecomeOrCeaseSchemePage, ContractsOrPoliciesPage, SchemeChangeDatePage}
import pages.event11.{HasSchemeChangedRulesInvestmentsInAssetsPage, InvestmentsInAssetsRuleChangeDatePage, UnAuthPaymentsRuleChangeDatePage}
import pages.event12.DateOfChangePage
import pages.event13.{ChangeDatePage, SchemeStructureDescriptionPage, SchemeStructurePage}
import pages.event19.{CountryOrTerritoryPage, DateChangeMadePage}
import pages.event2.{AmountPaidPage, DatePaidPage}
import pages.event24._
import pages.event6.{AmountCrystallisedAndDatePage, InputProtectionTypePage, TypeOfProtectionPage => Event6TypeOfProtectionPage}
import pages.event7.{CrystallisedAmountPage, LumpSumAmountPage, PaymentDatePage}
import pages.event8.{LumpSumAmountAndDatePage, TypeOfProtectionReferencePage, TypeOfProtectionPage => Event8TypeOfProtectionPage}
import pages.event8a.PaymentTypePage
import pages.{TaxYearPage, VersionInfoPage}
import play.api.libs.json.Writes
import utils.{CountryOptions, Event2MemberPageNumbers, InputOption}

import java.time.LocalDate

object SampleData extends SpecBase {
  private val options: Seq[InputOption] = Seq(
    InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"),
    InputOption("GB", "Great Britain")
  )

  def countryOptions: CountryOptions = new CountryOptions(options)


  val seqAddresses: Seq[Address] = Seq[Address](
    Address(
      addressLine1 = "addr11",
      addressLine2 = Some("addr12"),
      townOrCity = "addr13",
      county = Some("addr14"),
      postcode = Some("zz11zz"),
      country = "GB"
    ),
    Address(
      addressLine1 = "addr21",
      addressLine2 = Some("addr22"),
      townOrCity = "addr23",
      county = Some("addr24"),
      postcode = Some("zz11zz"),
      country = "GB"
    )
  )

  val tolerantAddressRequiredFieldsOnly: TolerantAddress = TolerantAddress(
      addressLine1 = Some("addr11"),
      addressLine2 = Some("addr13"),
      townOrCity = Some("ES"),
      county = None,
      postcode = None,
      countryOpt = None
    )

  val addressRequiredFieldsOnly: Address = Address(
    addressLine1 = "addr11",
    addressLine2 = None,
    townOrCity = "addr13",
    county = None,
    postcode = None,
    country = "ES"
  )

  val employerAddress: Address = Address(
    addressLine1 = "addr11",
    addressLine2 = Some("addr12"),
    townOrCity = "addr13",
    county = Some("addr14"),
    postcode = Some("zz11zz"),
    country = "GB"
  )

  val event1EmployerAddress: Address = Address(
    addressLine1 = "10 Other Place",
    addressLine2 = Some("Some District"),
    townOrCity = "Anytown",
    county = Some("Anyplace"),
    postcode = Some("ZZ1 1ZZ"),
    country = "GB"
  )

  val memberAddress: Address = Address(
    addressLine1 = "10 Other Place",
    addressLine2 = Some("Some District"),
    townOrCity = "Anytown",
    county = Some("Anyplace"),
    postcode = Some("ZZ1 1ZZ"),
    country = "GB"
  )

  val seqTolerantAddresses: Seq[TolerantAddress] = Seq[TolerantAddress](
    TolerantAddress(
      addressLine1 = Some("addr11"),
      addressLine2 = Some("addr12"),
      townOrCity = Some("addr13"),
      county = Some("addr14"),
      postcode = Some("zz11zz"),
      countryOpt = Some("GB")
    ),
    TolerantAddress(
      addressLine1 = Some("addr21"),
      addressLine2 = Some("addr22"),
      townOrCity = Some("addr23"),
      county = Some("addr24"),
      postcode = Some("zz11zz"),
      countryOpt = Some("GB")
    )
  )

  val startDate: LocalDate = LocalDate.of(2022, 11, 8)
  val companyDetails: CompanyDetails = CompanyDetails("Company Name", "12345678")
  val companyDetails2: CompanyDetails = CompanyDetails("Company Name 2", "12345679")

  val memberDetails: MembersDetails = MembersDetails("Joe", "Bloggs", "AA234567D")
  val memberDetails2: MembersDetails = MembersDetails("Steven", "Bloggs", "AA123456C")

  val memberDetailsEr1: MembersDetails = MembersDetails("Joe", "Bloggs", "AA123456A")
  val memberDetailsEr2: MembersDetails = MembersDetails("Joe", "Bloggs", "AA123456B")
  val memberDetails3: MembersDetails = MembersDetails("Joe", "Bloggs", "AA123456C")
  val memberDetails4: MembersDetails = MembersDetails("Joe", "Bloggs", "AA123456D")

  val memberDetails5: MembersDetails = MembersDetails("Joe", "Bloggs", "AA234567A")
  val memberDetails6: MembersDetails = MembersDetails("Joe", "Bloggs", "AA234567B")
  val memberDetails7: MembersDetails = MembersDetails("Joe", "Bloggs", "AA234567C")
  val memberDetails8: MembersDetails = MembersDetails("Joe", "Bloggs", "AA234567D")

  val memberDetails9: MembersDetails = MembersDetails("Joe", "Bloggs", "AA345678A")
  val memberDetails10: MembersDetails = MembersDetails("Joe", "Bloggs", "AA345678B")

  private val writesTaxYear: Writes[ChooseTaxYear] = ChooseTaxYear.writes(using ChooseTaxYear.enumerable(2021))

  val paymentDetails: Event1PaymentDetails = Event1PaymentDetails(1000.00, LocalDate.of(2022, 11, 8))
  val crystallisedDetails: CrystallisedDetails = CrystallisedDetails(10.00, LocalDate.of(2022, 11, 8))
  val crystallisedDetails2: CrystallisedDetails = CrystallisedDetails(10.00, LocalDate.of(2022, 8, 12))
  val lumpSumDetails = LumpSumDetails(10.00, LocalDate.of(2022, 3, 22))
  val paymentDetailsCommon: CommonPaymentDetails = CommonPaymentDetails(10.00, LocalDate.of(2022, 4, 5))

  val datePaid: LocalDate = LocalDate.of(2022, 3, 22)
  val amountPaid: BigDecimal = 999.11

  val event7PaymentDate: PaymentDate = PaymentDate(LocalDate.of(2022, 11, 8))

  val lumpSumAmountEvent7: BigDecimal = BigDecimal(100.00)
  val crystallisedAmountEvent7: BigDecimal = BigDecimal(50.00)
  val totalPaymentAmountEvent22and23: BigDecimal = BigDecimal(10.00)
  val crystallisedAmountEvent24: BigDecimal = BigDecimal(10.00)

  def booleanCYAVal(value: Boolean): String = if (value) "site.yes" else "site.no"

  val loanDetails: LoanDetails = LoanDetails(BigDecimal(10.00), BigDecimal(20.57))

  val schemeDetails: SchemeDetails = SchemeDetails(Some("SchemeName"), Some("SchemeReference"))

  val taxYear: ChooseTaxYear = ChooseTaxYear("2013")


  val userAnswersWithOneMemberAndEmployerEvent1: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"), true)
    .setOrException(WhoReceivedUnauthPaymentPage(0), Member)
    .setOrException(PaymentValueAndDatePage(0), Event1PaymentDetails(BigDecimal(857.00), LocalDate.of(2022, 11, 9)))
    .setOrException(MembersDetailsPage(Event1, 0), memberDetails)
    .setOrException(WhoReceivedUnauthPaymentPage(1), Employer)
    .setOrException(PaymentValueAndDatePage(1), Event1PaymentDetails(BigDecimal(7687.00), LocalDate.of(2022, 11, 9)))
    .setOrException(CompanyDetailsPage(1), companyDetails)

  val sampleMemberJourneyDataEvent1: UserAnswers = UserAnswers()
    .setOrException(ManualOrUploadPage(Event1, 0), Manual)
    .setOrException(WhoReceivedUnauthPaymentPage(0), Member)
    .setOrException(MembersDetailsPage(Event1, 0), memberDetails)
    .setOrException(DoYouHoldSignedMandatePage(0), false)
    .setOrException(ValueOfUnauthorisedPaymentPage(0), false)
    .setOrException(MemberPaymentNaturePage(0), BenefitInKind)
    .setOrException(BenefitInKindBriefDescriptionPage(0), "Test description")
    .setOrException(PaymentValueAndDatePage(0), paymentDetails)
    .setOrException(TaxYearPage, TaxYear("2022"), true)
    .setOrException(VersionInfoPage, VersionInfo(1, VersionStatus.Compiled), true)

  val sampleEmployerJourneyDataEvent1: UserAnswers = UserAnswers()
    .setOrException(ManualOrUploadPage(Event1, 0), Manual)
    .setOrException(WhoReceivedUnauthPaymentPage(0), Employer)
    .setOrException(CompanyDetailsPage(0), companyDetails)
    .setOrException(ManualAddressPage(Event1EmployerAddressJourney, 0, true), employerAddress)
    .setOrException(EmployerPaymentNaturePage(0), TangibleMoveableProperty)
    .setOrException(EmployerTangibleMoveablePropertyPage(0), "Another test description")
    .setOrException(PaymentValueAndDatePage(0), paymentDetails)

  val sampleMemberJourneyDataEvent2: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event2, 0, Event2MemberPageNumbers.FIRST_PAGE_DECEASED), memberDetails)
    .setOrException(MembersDetailsPage(Event2, 0, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY), memberDetails)
    .setOrException(AmountPaidPage(0, Event2), amountPaid)
    .setOrException(DatePaidPage(0, Event2), datePaid)

  val sampleMemberJourneyDataWithPaginationEvent2: UserAnswers =
    (0 to 25).foldLeft(emptyUserAnswersWithTaxYear) { (acc, i) =>
      acc.setOrException(MembersDetailsPage(Event2, i, Event2MemberPageNumbers.FIRST_PAGE_DECEASED), memberDetails)
        .setOrException(MembersDetailsPage(Event2, i, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY), memberDetails)
        .setOrException(AmountPaidPage(i, Event2), BigDecimal(10.00))
        .setOrException(DatePaidPage(i, Event2), datePaid)
    }

  def sampleMemberJourneyDataEvent3and4and5(eventType: EventType): UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"), true)
    .setOrException(VersionInfoPage, VersionInfo(1, VersionStatus.Compiled), true)
    .setOrException(MembersDetailsPage(eventType, 0), memberDetails)
    .setOrException(PaymentDetailsPage(eventType, 0), paymentDetailsCommon)

  def event345UADataWithPagnination(eventType: EventType): UserAnswers =
    (0 to 25).foldLeft(emptyUserAnswersWithTaxYear) { (acc, i) =>
      acc.setOrException(MembersDetailsPage(eventType, i), memberDetails)
        .setOrException(PaymentDetailsPage(eventType, i), paymentDetailsCommon)
    }

  val sampleMemberJourneyDataEvent6: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event6, 0), memberDetails)
    .setOrException(Event6TypeOfProtectionPage(Event6, 0), Event6TypeOfProtection.EnhancedLifetimeAllowance)
    .setOrException(InputProtectionTypePage(Event6, 0), "1234567A")
    .setOrException(AmountCrystallisedAndDatePage(Event6, 0), crystallisedDetails)

  val event6UADataWithPagination: UserAnswers =
    (0 to 25).foldLeft(emptyUserAnswersWithTaxYear) { (acc, i) =>
      acc.setOrException(MembersDetailsPage(Event6, i), memberDetails)
        .setOrException(Event6TypeOfProtectionPage(Event6, i), Event6TypeOfProtection.EnhancedLifetimeAllowance)
        .setOrException(InputProtectionTypePage(Event6, i), "1234567A")
        .setOrException(AmountCrystallisedAndDatePage(Event6, i), crystallisedDetails)
    }

  val event7UADataWithPagination: UserAnswers =
    (0 to 25).foldLeft(emptyUserAnswersWithTaxYear) { (acc, i) =>
      acc.setOrException(MembersDetailsPage(Event7, i), memberDetails)
        .setOrException(LumpSumAmountPage(i), lumpSumAmountEvent7)
        .setOrException(CrystallisedAmountPage(i), crystallisedAmountEvent7)
        .setOrException(PaymentDatePage(i), event7PaymentDate)
    }

  val sampleMemberJourneyDataEvent7: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event7, 0), memberDetails)
    .setOrException(LumpSumAmountPage(0), lumpSumAmountEvent7)
    .setOrException(CrystallisedAmountPage(0), crystallisedAmountEvent7)
    .setOrException(PaymentDatePage(0), event7PaymentDate)


  val sampleMemberJourneyDataEvent8: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event8, 0), memberDetails)
    .setOrException(Event8TypeOfProtectionPage(Event8, 0), Event8TypeOfProtection.PrimaryProtection)
    .setOrException(TypeOfProtectionReferencePage(Event8, 0), "1234567A")
    .setOrException(LumpSumAmountAndDatePage(Event8, 0), lumpSumDetails)

  val event8UADataWithPagination: UserAnswers =
    (0 to 25).foldLeft(emptyUserAnswersWithTaxYear) { (acc, i) =>
      acc.setOrException(MembersDetailsPage(Event8, i), memberDetails)
        .setOrException(Event8TypeOfProtectionPage(Event8, i), Event8TypeOfProtection.PrimaryProtection)
        .setOrException(TypeOfProtectionReferencePage(Event8, i), "1234567A")
        .setOrException(LumpSumAmountAndDatePage(Event8, i), lumpSumDetails)
    }

  val sampleMemberJourneyDataEvent8A: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event8A, 0), memberDetails)
    .setOrException(PaymentTypePage(Event8A, 0), PaymentType.PaymentOfAStandAloneLumpSum)
    .setOrException(Event8TypeOfProtectionPage(Event8A, 0), Event8TypeOfProtection.PrimaryProtection)
    .setOrException(TypeOfProtectionReferencePage(Event8A, 0), "1234567A")
    .setOrException(LumpSumAmountAndDatePage(Event8A, 0), lumpSumDetails)


  val event8aUADataWithPagination: UserAnswers =
    (0 to 25).foldLeft(emptyUserAnswersWithTaxYear) { (acc, i) =>
      acc.setOrException(MembersDetailsPage(Event8A, i), memberDetails)
        .setOrException(PaymentTypePage(Event8A, i), PaymentType.PaymentOfAStandAloneLumpSum)
        .setOrException(Event8TypeOfProtectionPage(Event8A, i), Event8TypeOfProtection.PrimaryProtection)
        .setOrException(TypeOfProtectionReferencePage(Event8A, i), "1234567A")
        .setOrException(LumpSumAmountAndDatePage(Event8A, i), lumpSumDetails)
    }

  val erOverviewSeq = Seq(EROverview(
    LocalDate.of(2022, 4, 6),
    LocalDate.of(2023, 4, 5),
    TaxYear("2022"),
    tpssReportPresent = true,
    Some(EROverviewVersion(
      3,
      submittedVersionAvailable = true,
      compiledVersionAvailable = false
    ))
  ),
    EROverview(
      LocalDate.of(2023, 4, 6),
      LocalDate.of(2024, 4, 5),
      TaxYear("2023"),
      tpssReportPresent = true,
      Some(EROverviewVersion(
        2,
        submittedVersionAvailable = true,
        compiledVersionAvailable = false
      ))
    ))
  def sampleMemberJourneyDataEvent22and23(eventType: EventType): UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(eventType, 0), memberDetails)
    .setOrException(ChooseTaxYearPage(eventType, 0), ChooseTaxYear("2015"))(writesTaxYear)
    .setOrException(TotalPensionAmountsPage(eventType, 0), BigDecimal(10.00))

  def sampleMemberJourneyDataEvent22and23WithMissingAmount(eventType: EventType): UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(eventType, 0), memberDetails)
    .setOrException(ChooseTaxYearPage(eventType, 0), ChooseTaxYear("2015"))(writesTaxYear)


  def event22and23UADataWithPagination(eventType: EventType) =
    (0 to 25).foldLeft(emptyUserAnswersWithTaxYear) { (acc, i) =>
      acc.setOrException(MembersDetailsPage(eventType, i), memberDetails)
        .setOrException(ChooseTaxYearPage(eventType, i), ChooseTaxYear("2015"))(writesTaxYear)
        .setOrException(TotalPensionAmountsPage(eventType, i), BigDecimal(10.00))
    }

  def sampleMemberJourneyDataEvent24(eventType: EventType): UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(eventType, 0), memberDetails)
    .setOrException(CrystallisedDatePage(0), CrystallisedDate(LocalDate.of(2023, 2, 12)))
    .setOrException(BCETypeSelectionPage(0), BCETypeSelection.StandAlone)
    .setOrException(TotalAmountBenefitCrystallisationPage(0), crystallisedAmountEvent24)
    .setOrException(ValidProtectionPage(0), true)
    .setOrException(pages.event24.TypeOfProtectionGroup1ReferencePage(0), ProtectionReferenceData("abc123DEF", "", "", ""))
    .setOrException(OverAllowancePage(0), false)
    .setOrException(OverAllowanceAndDeathBenefitPage(0), true)
    .setOrException(MarginalRatePage(0), true)
    .setOrException(EmployerPayeReferencePage(0), "123/ABCDE")

  def event24UADataWithPagination(eventType: EventType) =
    (0 to 25).foldLeft(emptyUserAnswersWithTaxYear) { (acc, i) =>
      acc.setOrException(MembersDetailsPage(eventType, i), memberDetails)
        .setOrException(ChooseTaxYearPage(eventType, i), ChooseTaxYear("2015"))(writesTaxYear)
        .setOrException(CrystallisedDatePage(i), CrystallisedDate(LocalDate.of(2023, 2, 12)))
        .setOrException(BCETypeSelectionPage(i), BCETypeSelection.StandAlone)
        .setOrException(TotalAmountBenefitCrystallisationPage(i), crystallisedAmountEvent24)
        .setOrException(ValidProtectionPage(i), true)
        .setOrException(pages.event24.TypeOfProtectionGroup1ReferencePage(i), ProtectionReferenceData("abc123DEF", "", "", ""))
        .setOrException(OverAllowancePage(i), false)
        .setOrException(OverAllowanceAndDeathBenefitPage(i), true)
        .setOrException(MarginalRatePage(i), true)
        .setOrException(EmployerPayeReferencePage(i), "123/ABCDE")
    }

  def sampleTwoMemberJourneyDataEvent22and23(eventType: EventType): UserAnswers =
    UserAnswers()
      .setOrException(MembersDetailsPage(eventType, 0), memberDetails)
      .setOrException(ChooseTaxYearPage(eventType, 0), taxYear)(writesTaxYear)
      .setOrException(TotalPensionAmountsPage(eventType, 0), totalPaymentAmountEvent22and23)
      .setOrException(MembersDetailsPage(eventType, 1), memberDetails2)
      .setOrException(ChooseTaxYearPage(eventType, 1), taxYear)(writesTaxYear)
      .setOrException(TotalPensionAmountsPage(eventType, 1), totalPaymentAmountEvent22and23)

  def sampleJourneyData10BecameAScheme: UserAnswers =
    UserAnswers()
      .setOrException(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItBecameAnInvestmentRegulatedPensionScheme)
      .setOrException(SchemeChangeDatePage, SchemeChangeDate(LocalDate.of(2022, 3, 22)))
      .setOrException(ContractsOrPoliciesPage, true)

  def sampleJourneyData10CeasedToBecomeAScheme: UserAnswers =
    UserAnswers()
      .setOrException(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItHasCeasedToBeAnInvestmentRegulatedPensionScheme)
      .setOrException(SchemeChangeDatePage, SchemeChangeDate(LocalDate.of(2022, 3, 22)))

  def sampleJourneyData11SchemeChangedBothRules: UserAnswers =
    UserAnswers().setOrException(pages.event11.HasSchemeChangedRulesPage, true)
      .setOrException(UnAuthPaymentsRuleChangeDatePage, Event11Date(LocalDate.of(2024, 4, 4)))
      .setOrException(HasSchemeChangedRulesInvestmentsInAssetsPage, true)
      .setOrException(InvestmentsInAssetsRuleChangeDatePage, Event11Date(LocalDate.of(2024, 4, 4)))

  def sampleJourneyData11SchemeNotChangedInAssets: UserAnswers =
    UserAnswers().setOrException(pages.event11.HasSchemeChangedRulesPage, true)
      .setOrException(UnAuthPaymentsRuleChangeDatePage, Event11Date(LocalDate.of(2024, 4, 4)))
      .setOrException(HasSchemeChangedRulesInvestmentsInAssetsPage, false)

  def sampleJourneyData11SchemeChangedRulesForAssetsOnly: UserAnswers =
    UserAnswers().setOrException(pages.event11.HasSchemeChangedRulesPage, false)
      .setOrException(HasSchemeChangedRulesInvestmentsInAssetsPage, true)
      .setOrException(InvestmentsInAssetsRuleChangeDatePage, Event11Date(LocalDate.of(2024, 4, 4)))

  def sampleJourneyData11SchemeNoChangedRules: UserAnswers =
    UserAnswers().setOrException(pages.event11.HasSchemeChangedRulesPage, false)
      .setOrException(HasSchemeChangedRulesInvestmentsInAssetsPage, false)

  def sampleEvent12JourneyData: UserAnswers =
    emptyUserAnswersWithTaxYear
      .setOrException(pages.event12.HasSchemeChangedRulesPage, true)
      .setOrException(DateOfChangePage, DateOfChange(LocalDate.of(2022, 3, 22)))

  def sampleEvent13JourneyData: UserAnswers =
    emptyUserAnswersWithTaxYear.setOrException(SchemeStructurePage, SchemeStructure.Other)
      .setOrException(SchemeStructureDescriptionPage, "foo")
      .setOrException(ChangeDatePage, LocalDate.of(2024, 4, 4))

  def sampleJourneyData19CountryOrTerritory: UserAnswers =
    UserAnswers()
      .setOrException(CountryOrTerritoryPage, seqAddresses.head.country)
      .setOrException(DateChangeMadePage, LocalDate.of(2022, 3, 22))

  def sampleEvent20JourneyData: UserAnswers =
    emptyUserAnswersWithTaxYear
      .setOrException(pages.event20.WhatChangePage, BecameOccupationalScheme)
      .setOrException(pages.event20.BecameDatePage, Event20Date(LocalDate.of(2023, 12, 12)))
      .setOrException(VersionInfoPage, VersionInfo(1, Compiled))

  def sampleEvent20ABecameJourneyData: UserAnswers =
    emptyUserAnswersWithTaxYear
      .setOrException(pages.event20A.WhatChangePage, BecameMasterTrust)
      .setOrException(pages.event20A.BecameDatePage, LocalDate.of(2023, 1, 12))
      .setOrException(VersionInfoPage, VersionInfo(1, Compiled))

  def sampleEvent20ACeasedJourneyData: UserAnswers =
    emptyUserAnswersWithTaxYear
      .setOrException(pages.event20A.WhatChangePage, CeasedMasterTrust)
      .setOrException(pages.event20A.CeasedDatePage, LocalDate.of(2023, 1, 12))

  def sampleEvent18JourneyData: UserAnswers =
    emptyUserAnswersWithTaxYear
      .setOrException(pages.event18.Event18ConfirmationPage, true)
}
