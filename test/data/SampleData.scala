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

package data

import models.address.{Address, TolerantAddress}
import models.common.ManualOrUpload.Manual
import models.common.{ChooseTaxYear, MembersDetails}
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event2, Event22, Event23, Event6, Event7, Event8, Event8A}
import models.event1.PaymentDetails
import models.event1.PaymentNature.BenefitInKind
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.employer.PaymentNature.TangibleMoveableProperty
import models.event1.employer.{CompanyDetails, LoanDetails}
import models.event1.member.SchemeDetails
import models.event6.{CrystallisedDetails, TypeOfProtection => Event6TypeOfProtection}
import models.event7.PaymentDate
import models.event8.{LumpSumDetails, TypeOfProtection => Event8TypeOfProtection}
import models.event8a.PaymentType
import models.{TaxYear, UserAnswers}
import pages.TaxYearPage
import pages.address.ManualAddressPage
import pages.common.{ChooseTaxYearPage, ManualOrUploadPage, MembersDetailsPage, TotalPensionAmountsPage}
import pages.event1._
import pages.event1.employer.{CompanyDetailsPage, EmployerTangibleMoveablePropertyPage, PaymentNaturePage => EmployerPaymentNaturePage}
import pages.event1.member.{BenefitInKindBriefDescriptionPage, PaymentNaturePage => MemberPaymentNaturePage}
import pages.event2.{AmountPaidPage, DatePaidPage}
import pages.event6.{AmountCrystallisedAndDatePage, InputProtectionTypePage, TypeOfProtectionPage => Event6TypeOfProtectionPage}
import pages.event7.{CrystallisedAmountPage, LumpSumAmountPage, PaymentDatePage}
import pages.event8.{LumpSumAmountAndDatePage, TypeOfProtectionReferencePage, TypeOfProtectionPage => Event8TypeOfProtectionPage}
import pages.event8a.PaymentTypePage
import utils.{CountryOptions, Event2MemberPageNumbers, InputOption}

import java.time.LocalDate

object SampleData {
  private val options: Seq[InputOption] = Seq(
    InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"),
    InputOption("GB", "Great Britain")
  )

  def countryOptions: CountryOptions = new CountryOptions(options)

  val seqAddresses: Seq[Address] = Seq[Address](
    Address(
      addressLine1 = "addr11",
      addressLine2 = "addr12",
      addressLine3 = Some("addr13"),
      addressLine4 = Some("addr14"),
      postcode = Some("zz11zz"),
      country = "GB"
    ),
    Address(
      addressLine1 = "addr21",
      addressLine2 = "addr22",
      addressLine3 = Some("addr23"),
      addressLine4 = Some("addr24"),
      postcode = Some("zz11zz"),
      country = "GB"
    )
  )

  val employerAddress: Address = Address(
    addressLine1 = "addr11",
    addressLine2 = "addr12",
    addressLine3 = Some("addr13"),
    addressLine4 = Some("addr14"),
    postcode = Some("zz11zz"),
    country = "GB"
  )

  val seqTolerantAddresses: Seq[TolerantAddress] = Seq[TolerantAddress](
    TolerantAddress(
      addressLine1 = Some("addr11"),
      addressLine2 = Some("addr12"),
      addressLine3 = Some("addr13"),
      addressLine4 = Some("addr14"),
      postcode = Some("zz11zz"),
      countryOpt = Some("GB")
    ),
    TolerantAddress(
      addressLine1 = Some("addr21"),
      addressLine2 = Some("addr22"),
      addressLine3 = Some("addr23"),
      addressLine4 = Some("addr24"),
      postcode = Some("zz11zz"),
      countryOpt = Some("GB")
    )
  )
  val companyDetails: CompanyDetails = CompanyDetails("Company Name", "12345678")

  val memberDetails: MembersDetails = MembersDetails("Joe", "Bloggs", "AA234567V")
  val memberDetails2: MembersDetails = MembersDetails("Steven", "Bloggs", "AA123456C")

  val paymentDetails: PaymentDetails = PaymentDetails(1000.00, LocalDate.of(2022, 11, 8))
  val crystallisedDetails: CrystallisedDetails = CrystallisedDetails(857.00, LocalDate.of(2022, 11, 8))
  val lumpSumDetails = LumpSumDetails(223.11, LocalDate.of(2022, 3, 22))

  val datePaid: LocalDate = LocalDate.of(2022,5,19)
  val amountPaid: BigDecimal = 999.11

  val event7PaymentDate: PaymentDate = PaymentDate(LocalDate.of(2022, 11, 8))
  val lumpSumAmount: BigDecimal = BigDecimal(100.00)
  val crystallisedAmount: BigDecimal = BigDecimal(50.00)

  def booleanCYAVal(value: Boolean): String = if (value) "site.yes" else "site.no"

  val loanDetails: LoanDetails = LoanDetails(Some(BigDecimal(10.00)), Some(BigDecimal(20.57)))

  val schemeDetails: SchemeDetails = SchemeDetails(Some("SchemeName"), Some("SchemeReference"))

  val taxYear: ChooseTaxYear = ChooseTaxYear("2013")

  val totalPaymentAmount: BigDecimal = BigDecimal(999.11)
  val totalPaymentAmountEvent23: BigDecimal = BigDecimal(1234.56)
  val totalPaymentAmountEvent23CurrencyFormat: String = "1,234.56"
  val userAnswersWithOneMemberAndEmployer: UserAnswers = UserAnswers()
    .setOrException(WhoReceivedUnauthPaymentPage(0), Member)
    .setOrException(PaymentValueAndDatePage(0), PaymentDetails(BigDecimal(857.00), LocalDate.of(2022, 11, 9)))
    .setOrException(MembersDetailsPage(Event1, 0), memberDetails)
    .setOrException(WhoReceivedUnauthPaymentPage(1), Employer)
    .setOrException(PaymentValueAndDatePage(1), PaymentDetails(BigDecimal(7687.00), LocalDate.of(2022, 11, 9)))
    .setOrException(CompanyDetailsPage(1), companyDetails)

  val sampleMemberJourneyData: UserAnswers = UserAnswers()
    .setOrException(ManualOrUploadPage(Event1, 0), Manual)
    .setOrException(WhoReceivedUnauthPaymentPage(0), Member)
    .setOrException(MembersDetailsPage(Event1, 0), memberDetails)
    .setOrException(DoYouHoldSignedMandatePage(0), false)
    .setOrException(ValueOfUnauthorisedPaymentPage(0), false)
    .setOrException(MemberPaymentNaturePage(0), BenefitInKind)
    .setOrException(BenefitInKindBriefDescriptionPage(0), "Test description")
    .setOrException(PaymentValueAndDatePage(0), paymentDetails)

  val sampleEmployerJourneyData: UserAnswers = UserAnswers()
    .setOrException(ManualOrUploadPage(Event1, 0), Manual)
    .setOrException(WhoReceivedUnauthPaymentPage(0), Employer)
    .setOrException(CompanyDetailsPage(0), companyDetails)
    .setOrException(ManualAddressPage(Event1EmployerAddressJourney, 0), employerAddress)
    .setOrException(EmployerPaymentNaturePage(0), TangibleMoveableProperty)
    .setOrException(EmployerTangibleMoveablePropertyPage(0), "Another test description")
    .setOrException(PaymentValueAndDatePage(0), paymentDetails)

  val sampleMemberJourneyDataEvent23: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event23, 0), memberDetails)
    .setOrException(ChooseTaxYearPage(Event23, 0), ChooseTaxYear("2015"))
    .setOrException(TotalPensionAmountsPage(Event23, 0), BigDecimal(1234.56))

  val sampleMemberJourneyDataEvent22: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event22, 0), memberDetails)
    .setOrException(ChooseTaxYearPage(Event22, 0), ChooseTaxYear("2018"))
    .setOrException(TotalPensionAmountsPage(Event22, 0), BigDecimal(999.11))

  val sampleMemberJourneyDataEvent6: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event6, 0), memberDetails)
    .setOrException(Event6TypeOfProtectionPage(Event6, 0), Event6TypeOfProtection.EnhancedLifetimeAllowance)
    .setOrException(InputProtectionTypePage(Event6, 0), "1234567A")
    .setOrException(AmountCrystallisedAndDatePage(Event6, 0), crystallisedDetails)

  val sampleMemberJourneyDataEvent2: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event2, 0, Event2MemberPageNumbers.FIRST_PAGE_DECEASED), memberDetails)
    .setOrException(MembersDetailsPage(Event2, 0, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY), memberDetails)
    .setOrException(AmountPaidPage(0, Event2), amountPaid)
    .setOrException(DatePaidPage(0, Event2), datePaid)

  val sampleMemberJourneyDataEvent7: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event7, 0), memberDetails)
    .setOrException(LumpSumAmountPage(0), lumpSumAmount)
    .setOrException(CrystallisedAmountPage(0), crystallisedAmount)
    .setOrException(PaymentDatePage(0),event7PaymentDate)


  val sampleMemberJourneyDataEvent8: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event8, 0), memberDetails)
    .setOrException(Event8TypeOfProtectionPage(Event8, 0), Event8TypeOfProtection.PrimaryProtection)
    .setOrException(TypeOfProtectionReferencePage(Event8, 0), "1234567A")
    .setOrException(LumpSumAmountAndDatePage(Event8, 0), lumpSumDetails)

  val sampleMemberJourneyDataEvent8A: UserAnswers = UserAnswers()
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(MembersDetailsPage(Event8A, 0), memberDetails)
    .setOrException(PaymentTypePage(Event8A, 0), PaymentType.PaymentOfAStandAloneLumpSum)
    .setOrException(Event8TypeOfProtectionPage(Event8A, 0), Event8TypeOfProtection.PrimaryProtection)
    .setOrException(TypeOfProtectionReferencePage(Event8A, 0), "1234567A")
    .setOrException(LumpSumAmountAndDatePage(Event8A, 0), lumpSumDetails)

  def sampleTwoMemberJourneyData(eventType: EventType): UserAnswers =
    UserAnswers()
      .setOrException(MembersDetailsPage(eventType, 0), memberDetails)
      .setOrException(ChooseTaxYearPage(eventType, 0), taxYear)
      .setOrException(TotalPensionAmountsPage(eventType, 0), totalPaymentAmount)
      .setOrException(MembersDetailsPage(eventType, 1), memberDetails2)
      .setOrException(ChooseTaxYearPage(eventType, 1), taxYear)
      .setOrException(TotalPensionAmountsPage(eventType, 1), totalPaymentAmount)

}
