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

package services.fileUpload

import base.SpecBase
import cats.data.Validated.{Invalid, Valid}
import config.FrontendAppConfig
import data.SampleData
import data.SampleData.countryOptions
import forms.address.ManualAddressFormProvider
import forms.common.MembersDetailsFormProvider
import forms.event1.employer.{CompanyDetailsFormProvider, LoanDetailsFormProvider, PaymentNatureFormProvider => employerPaymentNatureFormProvider, UnauthorisedPaymentRecipientNameFormProvider => EmployerUnauthorisedPaymentRecipientNameFormProvider}
import forms.event1.member._
import forms.event1.{PaymentNatureFormProvider => memberPaymentNatureFormProvider, _}
import models.common.MembersDetails
import models.enumeration.AddressJourneyType.{Event1EmployerAddressJourney, Event1EmployerPropertyAddressJourney, Event1MemberPropertyAddressJourney}
import models.enumeration.EventType.Event1
import models.event1.PaymentNature.{BenefitInKind, BenefitsPaidEarly, CourtOrConfiscationOrder, ErrorCalcTaxFreeLumpSums, MemberOther, OverpaymentOrWriteOff, RefundOfContributions, ResidentialPropertyHeld, TangibleMoveablePropertyHeld, TransferToNonRegPensionScheme}
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.employer.PaymentNature.{CourtOrder, EmployerOther, LoansExceeding50PercentOfFundValue, ResidentialProperty, TangibleMoveableProperty}
import models.event1.member.WhoWasTheTransferMade.AnEmployerFinanced
import models.event1.member.{ReasonForTheOverpaymentOrWriteOff, RefundOfContributions => RefundOfContributionsObject}
import models.{TaxYear, UserAnswers}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.TaxYearPage
import pages.address.ManualAddressPage
import pages.common.MembersDetailsPage
import pages.event1._
import pages.event1.employer.{CompanyDetailsPage, EmployerPaymentNatureDescriptionPage, EmployerTangibleMoveablePropertyPage, LoanDetailsPage, PaymentNaturePage => EmployerPaymentNaturePage, UnauthorisedPaymentRecipientNamePage => EmployerUnauthorisedPaymentRecipientNamePage}
import pages.event1.member._
import play.api.libs.json.Json
import services.fileUpload.ValidatorErrorMessages.HeaderInvalidOrFileIsEmpty
import utils.DateHelper

import java.time.LocalDate
import scala.collection.immutable.ArraySeq
import scala.util.chaining.scalaUtilChainingOps

class Event1ValidatorSpec extends SpecBase with Matchers with MockitoSugar with BeforeAndAfterEach {
  //scalastyle:off magic.number

  import Event1ValidatorSpec._

  override def beforeEach(): Unit = {
    Mockito.reset(mockFrontendAppConfig)
    when(mockFrontendAppConfig.validEvent1Header).thenReturn(header)
  }

  private val chainUaMembers: (UserAnswers, Int, MembersDetails, Boolean, Boolean, Boolean) =>
    UserAnswers = (ua, index, membersDetails, doYouHoldSignedMandate, valueOfUnauthorisedPayment, schemeUnAuthPaySurcharge) => {

    (valueOfUnauthorisedPayment, schemeUnAuthPaySurcharge) match {
      case (true, _) =>
        ua
          .setOrException(WhoReceivedUnauthPaymentPage(index).path, Json.toJson(Member.toString))
          .setOrException(MembersDetailsPage(Event1, index).path, Json.toJson(membersDetails))
          .setOrException(DoYouHoldSignedMandatePage(index).path, Json.toJson(doYouHoldSignedMandate))
          .setOrException(ValueOfUnauthorisedPaymentPage(index).path, Json.toJson(valueOfUnauthorisedPayment))
          .setOrException(SchemeUnAuthPaySurchargeMemberPage(index).path, Json.toJson(schemeUnAuthPaySurcharge))
      case _ =>
        ua
          .setOrException(WhoReceivedUnauthPaymentPage(index).path, Json.toJson(Member.toString))
          .setOrException(MembersDetailsPage(Event1, index).path, Json.toJson(membersDetails))
          .setOrException(DoYouHoldSignedMandatePage(index).path, Json.toJson(doYouHoldSignedMandate))
          .setOrException(ValueOfUnauthorisedPaymentPage(index).path, Json.toJson(valueOfUnauthorisedPayment))
    }

  }

  private val chainUaEmployers: (UserAnswers, Int) => UserAnswers = (ua, index) => {
    ua
      .setOrException(WhoReceivedUnauthPaymentPage(index).path, Json.toJson(Employer.toString))
      .setOrException(CompanyDetailsPage(index).path, Json.toJson(SampleData.companyDetails))
      .setOrException(ManualAddressPage(Event1EmployerAddressJourney, index).path, Json.toJson(SampleData.event1EmployerAddress))
      .setOrException(PaymentValueAndDatePage(index).path, Json.toJson(SampleData.paymentDetails))
  }

  "Event 1 validator" - {
    "must return items in user answers when there are no validation errors for Member" in {
      val validCSVFile = CSVParser.split(
        s"""$header
                                member,Joe,Bloggs,AA234567D,YES,YES,YES,,,,Benefit,Description,,,,,,,,,,,,,1000.00,08/11/2022
                                member,Steven,Bloggs,AA123456C,YES,YES,NO,,,,Transfer,,,,,,,,,,,,EMPLOYER,"SchemeName,SchemeReference",1000.00,08/11/2022
                                member,Joe,Bloggs,AA234567D,YES,NO,,,,,Error,,,,Description,,,,,,,,,,1000.00,08/11/2022
                                member,Steven,Bloggs,AA123456C,YES,YES,NO,,,,Early,,,Description,,,,,,,,,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA234567D,YES,YES,YES,,,,Refund,,,,,,,,,WIDOW/ORPHAN,,,,,1000.00,08/11/2022
                                member,Steven,Bloggs,AA123456C,YES,YES,NO,,,,Overpayment,,,,,,,,NO LONGER QUALIFIED,,,,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA234567D,YES,YES,NO,,,,Residential,,,,,,,,,,"10 Other Place,Some District,Anytown,Anyplace,ZZ1 1ZZ,GB",,,,1000.00,08/11/2022
                                member,Steven,Bloggs,AA123456C,YES,YES,YES,,,,Tangible,,,,,,,,,,,Description,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA234567D,YES,YES,YES,,,,Court,,John,,,,,,,,,,,,1000.00,08/11/2022
                                member,Steven,Bloggs,AA123456C,YES,NO,,,,,Other,,,,,,,Description,,,,,,,1000.00,08/11/2022"""
      )
      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
      val result = validator.validate(validCSVFile, ua)
      result mustBe Valid(ua
        .pipe(chainUaMembers(_, 0, SampleData.memberDetails, true, true, true))
        .setOrException(PaymentNaturePage(0).path, Json.toJson(BenefitInKind.toString))
        .setOrException(BenefitInKindBriefDescriptionPage(0).path, Json.toJson("Description"))
        .setOrException(PaymentValueAndDatePage(0).path, Json.toJson(SampleData.paymentDetails))

        .pipe(chainUaMembers(_, 1, SampleData.memberDetails2, true, true, false))
        .setOrException(PaymentNaturePage(1).path, Json.toJson(TransferToNonRegPensionScheme.toString))
        .setOrException(WhoWasTheTransferMadePage(1).path, Json.toJson(AnEmployerFinanced.toString))
        .setOrException(SchemeDetailsPage(1).path, Json.toJson(SampleData.schemeDetails))
        .setOrException(PaymentValueAndDatePage(1).path, Json.toJson(SampleData.paymentDetails))

        .pipe(chainUaMembers(_, 2, SampleData.memberDetails, true, false, true))
        .setOrException(PaymentNaturePage(2).path, Json.toJson(ErrorCalcTaxFreeLumpSums.toString))
        .setOrException(ErrorDescriptionPage(2).path, Json.toJson("Description"))
        .setOrException(PaymentValueAndDatePage(2).path, Json.toJson(SampleData.paymentDetails))

        .pipe(chainUaMembers(_, 3, SampleData.memberDetails2, true, true, false))
        .setOrException(PaymentNaturePage(3).path, Json.toJson(BenefitsPaidEarly.toString))
        .setOrException(BenefitsPaidEarlyPage(3).path, Json.toJson("Description"))
        .setOrException(PaymentValueAndDatePage(3).path, Json.toJson(SampleData.paymentDetails))

        .pipe(chainUaMembers(_, 4, SampleData.memberDetails, true, true, true))
        .setOrException(PaymentNaturePage(4).path, Json.toJson(RefundOfContributions.toString))
        .setOrException(RefundOfContributionsPage(4).path, Json.toJson(RefundOfContributionsObject.WidowOrOrphan.toString))
        .setOrException(PaymentValueAndDatePage(4).path, Json.toJson(SampleData.paymentDetails))

        .pipe(chainUaMembers(_, 5, SampleData.memberDetails2, true, true, false))
        .setOrException(PaymentNaturePage(5).path, Json.toJson(OverpaymentOrWriteOff.toString))
        .setOrException(ReasonForTheOverpaymentOrWriteOffPage(5).path, Json.toJson(ReasonForTheOverpaymentOrWriteOff.DependentNoLongerQualifiedForPension.toString))
        .setOrException(PaymentValueAndDatePage(5).path, Json.toJson(SampleData.paymentDetails))

        .pipe(chainUaMembers(_, 6, SampleData.memberDetails, true, true, false))
        .setOrException(PaymentNaturePage(6).path, Json.toJson(ResidentialPropertyHeld.toString))
        .setOrException(ManualAddressPage(Event1MemberPropertyAddressJourney, 6).path, Json.toJson(SampleData.memberAddress))
        .setOrException(PaymentValueAndDatePage(6).path, Json.toJson(SampleData.paymentDetails))

        .pipe(chainUaMembers(_, 7, SampleData.memberDetails2, true, true, true))
        .setOrException(PaymentNaturePage(7).path, Json.toJson(TangibleMoveablePropertyHeld.toString))
        .setOrException(MemberTangibleMoveablePropertyPage(7).path, Json.toJson("Description"))
        .setOrException(PaymentValueAndDatePage(7).path, Json.toJson(SampleData.paymentDetails))

        .pipe(chainUaMembers(_, 8, SampleData.memberDetails, true, true, true))
        .setOrException(PaymentNaturePage(8).path, Json.toJson(CourtOrConfiscationOrder.toString))
        .setOrException(UnauthorisedPaymentRecipientNamePage(8).path, Json.toJson("John"))
        .setOrException(PaymentValueAndDatePage(8).path, Json.toJson(SampleData.paymentDetails))

        .pipe(chainUaMembers(_, 9, SampleData.memberDetails2, true, false, true))
        .setOrException(PaymentNaturePage(9).path, Json.toJson(MemberOther.toString))
        .setOrException(MemberPaymentNatureDescriptionPage(9).path, Json.toJson("Description"))
        .setOrException(PaymentValueAndDatePage(9).path, Json.toJson(SampleData.paymentDetails))
      )
    }

    "must return items in user answers when there are no validation errors for Employer" in {
      //      val commonUaEmployer = """employer,,,,,,,Company Name,12345678,"10 Other Place,Some District,Anytown,Anyplace,ZZ1 1ZZ,GB"""
      val validCSVFile = CSVParser.split(
        s"""$header
                                employer,,,,,,,Company Name,12345678,"10 Other Place,Some District,Anytown,Anyplace,ZZ1 1ZZ,GB",Loans,,,,,10.00,20.57,,,,,,,,1000.00,08/11/2022
                                employer,,,,,,,Company Name,12345678,"10 Other Place,Some District,Anytown,Anyplace,ZZ1 1ZZ,GB",Residential,,,,,,,,,,"10 Other Place,Some District,Anytown,Anyplace,ZZ1 1ZZ,GB",,,,1000.00,08/11/2022
                                employer,,,,,,,Company Name,12345678,"10 Other Place,Some District,Anytown,Anyplace,ZZ1 1ZZ,GB",Tangible,,,,,,,,,,,,,,1000.00,08/11/2022
                                employer,,,,,,,Company Name,12345678,"10 Other Place,Some District,Anytown,Anyplace,ZZ1 1ZZ,GB",Court,,Organisation Name,,,,,,,,,,,,1000.00,08/11/2022
                                employer,,,,,,,Company Name,12345678,"10 Other Place,Some District,Anytown,Anyplace,ZZ1 1ZZ,GB",Other,,,,,,,Description,,,,,,,1000.00,08/11/2022"""
      )
      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
      val result = validator.validate(validCSVFile, ua)
      result mustBe Valid(ua
        .pipe(chainUaEmployers(_, 0))
        .setOrException(EmployerPaymentNaturePage(0).path, Json.toJson(LoansExceeding50PercentOfFundValue.toString))
        .setOrException(LoanDetailsPage(0).path, Json.toJson(SampleData.loanDetails))

        .pipe(chainUaEmployers(_, 1))
        .setOrException(EmployerPaymentNaturePage(1).path, Json.toJson(ResidentialProperty.toString))
        .setOrException(ManualAddressPage(Event1EmployerPropertyAddressJourney, 1).path, Json.toJson(SampleData.event1EmployerAddress))

        .pipe(chainUaEmployers(_, 2))
        .setOrException(EmployerPaymentNaturePage(2).path, Json.toJson(TangibleMoveableProperty.toString))
        .setOrException(EmployerTangibleMoveablePropertyPage(2).path, Json.toJson(None))

        .pipe(chainUaEmployers(_, 3))
        .setOrException(EmployerPaymentNaturePage(3).path, Json.toJson(CourtOrder.toString))
        .setOrException(EmployerUnauthorisedPaymentRecipientNamePage(3).path, Json.toJson("Organisation Name"))

        .pipe(chainUaEmployers(_, 4))
        .setOrException(EmployerPaymentNaturePage(4).path, Json.toJson(EmployerOther.toString))
        .setOrException(EmployerPaymentNatureDescriptionPage(4).path, Json.toJson("Description"))
      )
    }

    "return validation error for incorrect header" in {
      val csvFile = CSVParser.split("""test""")
      val result = validator.validate(csvFile, UserAnswers())
      result mustBe Invalid(Seq(
        ValidationError(0, 0, HeaderInvalidOrFileIsEmpty)
      ))
    }

    "return validation error for empty file" in {
      val result = validator.validate(Nil, UserAnswers())
      result mustBe Invalid(Seq(
        ValidationError(0, 0, HeaderInvalidOrFileIsEmpty)
      ))
    }

    "return validation errors when present, including tax year in future" in {
      DateHelper.setDate(Some(LocalDate.of(2022, 6, 1)))
      val csvFile = CSVParser.split(
        s"""$header
                        dsfgsd*,Joe,Bloggs,AA234567D,YES,YES,YES,,,,Benefit,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,,Bloggs12213,AA234567Dasdfsdf,YES,YES,YES,,,,Benefit,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA234567D,,,YES,,,,Benefit,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA234567D,YES,YES,,,,,Benefit,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        ,Joe,Bloggs,AA234567D,YES,YES,YES,,,,Benefit,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA234567D,YES,YES,YES,,,,,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA234567D,YES,YES,YES,,,,Benefit,DescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescriptionDescription,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Steven,Bloggs,AA123456C,YES,YES,NO,,,,Transfer,,,,,,,,,,,,,"SchemeName,SchemeReference",1000.00,08/11/2022
                        member,Joe,Bloggs,AA234567D,YESasdf,YES,YES,,,,Benefit,Description,,,,,,,,,,,,,1000.00,08/11/2022"""

      )
      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)

      val result = validator.validate(csvFile, ua)
      result mustBe Invalid(Seq(
        ValidationError(1, 0, "whoReceivedUnauthPayment.error.format", "memberOrEmployer"),
        ValidationError(2, 1, "membersDetails.error.firstName.required", "firstName"),
        ValidationError(2, 2, "membersDetails.error.lastName.invalid", "lastName", ArraySeq("""^[a-zA-Z &`\-\'\.^]{1,35}$""")),
        ValidationError(2, 3, "membersDetails.error.nino.invalid", "nino"),
        ValidationError(3, 4, "doYouHoldSignedMandate.error.required", "doYouHoldSignedMandate"),
        ValidationError(3, 5, "valueOfUnauthorisedPayment.error.required", "valueOfUnauthorisedPayment"),
        ValidationError(4, 6, "schemeUnAuthPaySurchargeMember.error.required", "schemeUnAuthPaySurcharge"),
        ValidationError(5, 0, "whoReceivedUnauthPayment.error.required", "memberOrEmployer"),
        ValidationError(6, 10, "paymentNature.error.required", "natureOfPayment"),
        ValidationError(7, 11, "benefitInKindBriefDescription.error.length", "benefitDescription", ArraySeq(150)),
        ValidationError(8, 22, "whoWasTheTransferMade.error.required", "transferMadeTo"),
        ValidationError(9, 4, "doYouHoldSignedMandate.error.format", "doYouHoldSignedMandate")

        //TODO: Come back to payment nature validation showing paymentNature.error.required instead of paymentNature.error.format
        //ValidationError(7, 10, "paymentNature.error.format", "natureOfPayment")
      ))
    }

  }

}

object Event1ValidatorSpec {
  private val header = "Member or employer," +
    "Member: first name,Member: last name,Member: National Insurance number," +
    "Member: Do you hold a signed mandate from the member to deduct tax from their unauthorised payment? (YES/NO)," +
    "Member: Is the value of the unauthorised payment more than 25% of the pension fund for the individual? (YES/NO)," +
    "Member: Is the scheme paying the unauthorised payment surcharge on behalf of the member? (YES/NO)," +
    "Employer: company or organisation name," +
    "Employer: company number," +
    "Employer: company address," +
    "Member and employer: Nature of the unauthorised payment or deemed unauthorised payment (see instructions for details)," +
    "If BENEFIT: Give a brief description (up to 150 characters)," +
    "If COURT: What is the name of the person or organisation that received the unauthorised payment? (see instructions for details)," +
    "If EARLY: Give a brief description (up to 150 characters)," +
    "If ERROR: Give a brief description (up to 150 characters)," +
    "If LOANS: Amount of the loan (£)," +
    "If LOANS: Value of the fund (£)," +
    "If OTHER: Give a brief description (up to 150 characters)," +
    "If OVERPAYMENT: What is the reason for the overpayment/write off? (see instructions for details)," +
    "If REFUND: Who received the fund? (see instructions for details)," +
    "If RESIDENTIAL: What is the address of the residential property? (see instructions for details)," +
    "If TANGIBLE: Give a brief description (up to 150 characters)," +
    "If TRANSFER: Who was the transfer was made to? (see instructions for details)," +
    "If TRANSFER: What are the scheme details? (see instructions for details)," +
    "Member and Employer: Total value or amount of the unauthorised payment (£)," +
    "Member and employer: Date of payment or when benefit made available (see instructions for details)"

  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  private val whoReceivedUnauthPaymentFormProvider = new WhoReceivedUnauthPaymentFormProvider
  private val membersDetailsFormProvider = new MembersDetailsFormProvider
  private val doYouHoldSignedMandateFormProvider = new DoYouHoldSignedMandateFormProvider
  private val valueOfUnauthorisedPaymentFormProvider = new ValueOfUnauthorisedPaymentFormProvider
  private val schemeUnAuthPaySurchargeMemberFormProvider = new SchemeUnAuthPaySurchargeMemberFormProvider
  private val memberPaymentNatureFormProvider = new memberPaymentNatureFormProvider
  private val benefitInKindBriefDescriptionFormProvider = new BenefitInKindBriefDescriptionFormProvider
  private val paymentValueAndDateFormProvider = new PaymentValueAndDateFormProvider
  private val whoWasTheTransferMadeFormProvider = new WhoWasTheTransferMadeFormProvider
  private val schemeDetailsFormProvider = new SchemeDetailsFormProvider
  private val errorDescriptionFormProvider = new ErrorDescriptionFormProvider
  private val benefitsPaidEarlyFormProvider = new BenefitsPaidEarlyFormProvider
  private val refundOfContributionsFormProvider = new RefundOfContributionsFormProvider
  private val reasonForTheOverpaymentOrWriteOffFormProvider = new ReasonForTheOverpaymentOrWriteOffFormProvider
  private val manualAddressFormProvider = new ManualAddressFormProvider(countryOptions)
  private val memberTangibleMoveablePropertyFormProvider = new MemberTangibleMoveablePropertyFormProvider
  private val unauthorisedPaymentRecipientNameFormProvider = new UnauthorisedPaymentRecipientNameFormProvider
  private val memberPaymentNatureDescriptionFormProvider = new MemberPaymentNatureDescriptionFormProvider
  private val companyDetailsFormProvider = new CompanyDetailsFormProvider
  private val employerPaymentNatureFormProvider = new employerPaymentNatureFormProvider
  private val loanDetailsFormProvider = new LoanDetailsFormProvider
  private val employerTangibleMoveablePropertyFormProvider = new EmployerTangibleMoveablePropertyFormProvider
  private val employerUnauthorisedPaymentRecipientNameFormProvider = new EmployerUnauthorisedPaymentRecipientNameFormProvider
  private val employerPaymentNatureDescriptionFormProvider = new EmployerPaymentNatureDescriptionFormProvider

  private val validator = new Event1Validator(
    whoReceivedUnauthPaymentFormProvider,
    membersDetailsFormProvider,
    doYouHoldSignedMandateFormProvider,
    paymentValueAndDateFormProvider,
    valueOfUnauthorisedPaymentFormProvider,
    schemeUnAuthPaySurchargeMemberFormProvider,
    memberPaymentNatureFormProvider,
    benefitInKindBriefDescriptionFormProvider,
    whoWasTheTransferMadeFormProvider,
    schemeDetailsFormProvider,
    errorDescriptionFormProvider,
    benefitsPaidEarlyFormProvider,
    refundOfContributionsFormProvider,
    reasonForTheOverpaymentOrWriteOffFormProvider,
    manualAddressFormProvider,
    memberTangibleMoveablePropertyFormProvider,
    unauthorisedPaymentRecipientNameFormProvider,
    memberPaymentNatureDescriptionFormProvider,
    companyDetailsFormProvider,
    employerPaymentNatureFormProvider,
    loanDetailsFormProvider,
    employerTangibleMoveablePropertyFormProvider,
    employerUnauthorisedPaymentRecipientNameFormProvider,
    employerPaymentNatureDescriptionFormProvider,
    mockFrontendAppConfig)
}