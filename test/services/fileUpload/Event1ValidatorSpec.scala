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
import models.enumeration.EventType.Event1
import models.event1.PaymentNature.BenefitInKind
import models.event1.WhoReceivedUnauthPayment.Member
import models.{TaxYear, UserAnswers}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.TaxYearPage
import pages.common.MembersDetailsPage
import pages.event1._
import pages.event1.member.{BenefitInKindBriefDescriptionPage, PaymentNaturePage}
import play.api.libs.json.Json
import services.fileUpload.ValidatorErrorMessages.HeaderInvalidOrFileIsEmpty

class Event1ValidatorSpec extends SpecBase with Matchers with MockitoSugar with BeforeAndAfterEach {
  //scalastyle:off magic.number

  import Event1ValidatorSpec._

  override def beforeEach(): Unit = {
    Mockito.reset(mockFrontendAppConfig)
    when(mockFrontendAppConfig.validEvent1Header).thenReturn(header)
  }

  "Event 1 validator" - {
    "must return items in user answers when there are no validation errors for Member" in {
      val validCSVFile = CSVParser.split(
        s"""$header
                      member,Joe,Bloggs,AA234567D,YES,YES,YES,,,,Benefit,Description,,,,,,,,,,,,,1000.00,08/11/2022"""
      )
      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
      val result = validator.validate(validCSVFile, ua)
      result mustBe Valid(ua
        .setOrException(WhoReceivedUnauthPaymentPage(0).path, Json.toJson(Member.toString))
        .setOrException(MembersDetailsPage(Event1, 0).path, Json.toJson(SampleData.memberDetails))
        .setOrException(DoYouHoldSignedMandatePage(0).path, Json.toJson(true))
        .setOrException(ValueOfUnauthorisedPaymentPage(0).path, Json.toJson(true))
        .setOrException(SchemeUnAuthPaySurchargeMemberPage(0).path, Json.toJson(true))
        .setOrException(PaymentNaturePage(0).path, Json.toJson(BenefitInKind.toString))
        .setOrException(BenefitInKindBriefDescriptionPage(0).path, Json.toJson("Description"))
        .setOrException(PaymentValueAndDatePage(0).path, Json.toJson(SampleData.paymentDetails))
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

    //    "return validation errors when present, including tax year in future" in {
    //      DateHelper.setDate(Some(LocalDate.of(2022, 6, 1)))
    //      val csvFile = CSVParser.split(
    //        s"""$header
    //            member,,Bloggs,AA234567D,YES,YES,YES,,,,Benefit,Description,,,,,,,,,,,,,1000.00,08/11/2022"""
    //
    //      )
    //      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
    //
    //      val result = validator.validate(csvFile, ua)
    //      result mustBe Invalid(Seq(
    //        ValidationError(1, 2, "membersDetails.error.firstName.required", "firstName")
    //      ))
    //    }
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
