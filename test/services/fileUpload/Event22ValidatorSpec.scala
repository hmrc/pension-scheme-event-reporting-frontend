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
import cats.data.Validated.Valid
import config.FrontendAppConfig
import data.SampleData
import data.SampleData.startDate
import forms.common.{ChooseTaxYearFormProvider, MembersDetailsFormProvider, TotalPensionAmountsFormProvider}
import models.UserAnswers
import models.common.ChooseTaxYear
import models.enumeration.EventType.Event22
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.common.{ChooseTaxYearPage, MembersDetailsPage, TotalPensionAmountsPage}
import play.api.libs.json.Json

class Event22ValidatorSpec extends SpecBase with Matchers with MockitoSugar with BeforeAndAfterEach {
  //scalastyle:off magic.number

  import Event22ValidatorSpec._

  override def beforeEach(): Unit = {
    Mockito.reset(mockFrontendAppConfig)
    when(mockFrontendAppConfig.validEvent22Header).thenReturn(header)
  }
  
  /*
  private val membersDetailsFormProvider = new MembersDetailsFormProvider
  private val chooseTaxYearFormProvider = new ChooseTaxYearFormProvider
  private val totalPensionAmountsFormProvider = new TotalPensionAmountsFormProvider
   */

  "Event 22 validator" - {
    "return items in user answers when there are no validation errors" in {
      val validCSVFile = CSVParser.split(
        s"""$header
                            Joe,Bloggs,AA234567V,06/04/2020,12.20
                            Steven,Bloggs,AA123456C,06/04/2022,13.20"""
      )
      val result = validator.parse(startDate, validCSVFile, UserAnswers())
      result mustBe Valid(UserAnswers()
        .setOrException(MembersDetailsPage(Event22, 0).path, Json.toJson(SampleData.memberDetails))
        .setOrException(ChooseTaxYearPage(Event22, 0).path, Json.toJson(ChooseTaxYear("2020")))
        .setOrException(TotalPensionAmountsPage(Event22, 0).path, Json.toJson(BigDecimal(12.20)))
        .setOrException(MembersDetailsPage(Event22, 1).path, Json.toJson(SampleData.memberDetails2))
        .setOrException(ChooseTaxYearPage(Event22, 1).path, Json.toJson(ChooseTaxYear("2022")))
        .setOrException(TotalPensionAmountsPage(Event22, 1).path, Json.toJson(BigDecimal(13.20)))
      )
    }

//    "return validation error for incorrect header" in {
//      val GivingIncorrectHeader = CsvLineSplitter.split("""test""")
//      val result = validator.parse(startDate, GivingIncorrectHeader, UserAnswers())
//      result mustBe Invalid(Seq(
//        ValidatorValidationError(0, 0, HeaderInvalidOrFileIsEmpty)
//      ))
//    }
//
//    "return validation error for empty file" in {
//      val result = validator.parse(startDate, Nil, UserAnswers())
//      result mustBe Invalid(Seq(
//        ValidatorValidationError(0, 0, HeaderInvalidOrFileIsEmpty)
//      ))
//    }
//
//    "return validation errors for member details when present" in {
//      val GivingInvalidMemberDetails = CsvLineSplitter.split(
//        s"""$header
//                            ,last,AB123456C,01/04/2000,123123,01/04/2020,1.00,2.00
//                            Joe,,123456C,01/04/2000,123123,01/04/2020,1.00,2.00"""
//      )
//
//      val result = validator.parse(startDate, GivingInvalidMemberDetails, UserAnswers())
//      result mustBe Invalid(Seq(
//        ValidatorValidationError(1, 0, "memberDetails.error.firstName.required", "firstName"),
//        ValidatorValidationError(2, 1, "memberDetails.error.lastName.required", "lastName"),
//        ValidatorValidationError(2, 2, "memberDetails.error.nino.invalid", "nino")
//      ))
//    }
//
//    "return validation errors for charge details when present, including missing year and missing month" in {
//      val GivingInvalidChargeDetails = CsvLineSplitter.split(
//        s"""$header
//                                first,last,AB123456C,01,123123,01/04,1.00,2.00
//                                Joe,Bloggs,AB123456C,01/04,123123,01,1.00,2.00"""
//      )
//
//      val result = validator.parse(startDate, GivingInvalidChargeDetails, UserAnswers())
//      result mustBe Invalid(Seq(
//        ValidatorValidationError(1, 3, "dob.error.incomplete", "dob", Seq("month", "year")),
//        ValidatorValidationError(1, 5, "chargeG.chargeDetails.qropsTransferDate.error.required.two", "qropsTransferDate", Seq("year")),
//        ValidatorValidationError(2, 3, "dob.error.incomplete", "dob", Seq("year")),
//        ValidatorValidationError(2, 5, "chargeG.chargeDetails.qropsTransferDate.error.required.two", "qropsTransferDate", Seq("month", "year"))
//      ))
//    }
//
//    "return validation errors for member details AND charge details AND charge amounts when all present" in {
//      val GivingInvalidMemberDetailsAndChargeDetails = CsvLineSplitter.split(
//        s"""$header
//                            ,last,AB123456C,01,123123,01/04,A,2.00
//                            Joe,,123456C,01/04,123123,01,1.00,B"""
//      )
//
//      val result = validator.parse(startDate, GivingInvalidMemberDetailsAndChargeDetails, UserAnswers())
//      result mustBe Invalid(Seq(
//        ValidatorValidationError(1, 0, "memberDetails.error.firstName.required", "firstName"),
//        ValidatorValidationError(1, 3, "dob.error.incomplete", "dob", Seq("month", "year")),
//        ValidatorValidationError(1, 5, "chargeG.chargeDetails.qropsTransferDate.error.required.two", "qropsTransferDate", Seq("year")),
//        ValidatorValidationError(1, 6, "The amount transferred into the QROPS for last must be an amount of money, like 123 or 123.45", "amountTransferred"),
//        ValidatorValidationError(2, 1, "memberDetails.error.lastName.required", "lastName"),
//        ValidatorValidationError(2, 3, "dob.error.incomplete", "dob", Seq("year")),
//        ValidatorValidationError(2, 2, "memberDetails.error.nino.invalid", "nino"),
//        ValidatorValidationError(2, 5, "chargeG.chargeDetails.qropsTransferDate.error.required.two", "qropsTransferDate", Seq("month", "year")),
//        ValidatorValidationError(2, 7, "amountTaxDue.error.invalid", "amountTaxDue")
//      ))
//    }
//
//    "return validation errors for charge amounts when present" in {
//
//      val GivingInvalidChargeAmounts = CsvLineSplitter.split(
//        s"""$header
//                            first,last,AB123456C,01/04/2000,123123,01/04/2020,,2.00
//                            Joe,Bloggs,AB123456C,01/04/2000,123123,01/04/2020,1.00,A"""
//      )
//
//      val result = validator.parse(startDate, GivingInvalidChargeAmounts, UserAnswers())
//      result mustBe Invalid(Seq(
//        ValidatorValidationError(1, 6, "Enter the amount transferred into the QROPS for first last", "amountTransferred"),
//        ValidatorValidationError(2, 7, "amountTaxDue.error.invalid", "amountTaxDue")
//      ))
//    }
  }

}

object Event22ValidatorSpec {
  private val header = "First name,Last name,National Insurance number," +
    "In which tax year was the annual allowance exceeded? (XXXX to XXXX)," +
    "What is the total of the member's pension input amounts for all arrangements under the scheme in the tax year that the annual allowance was exceeded? (£)"
  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  private val membersDetailsFormProvider = new MembersDetailsFormProvider
  private val chooseTaxYearFormProvider = new ChooseTaxYearFormProvider
  private val totalPensionAmountsFormProvider = new TotalPensionAmountsFormProvider

  private val validator = new Event22Validator(membersDetailsFormProvider, chooseTaxYearFormProvider, totalPensionAmountsFormProvider, mockFrontendAppConfig)
}
