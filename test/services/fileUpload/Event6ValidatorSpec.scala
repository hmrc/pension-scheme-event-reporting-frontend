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
import forms.common.MembersDetailsFormProvider
import forms.event6.{AmountCrystallisedAndDateFormProvider, InputProtectionTypeFormProvider, TypeOfProtectionFormProvider}
import models.enumeration.EventType.Event6
import models.event6.TypeOfProtection
import models.{TaxYear, UserAnswers}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.TaxYearPage
import pages.common.MembersDetailsPage
import pages.event6.{AmountCrystallisedAndDatePage, InputProtectionTypePage, TypeOfProtectionPage}
import play.api.libs.json.Json
import services.fileUpload.ValidatorErrorMessages.HeaderInvalidOrFileIsEmpty
import utils.DateHelper

import java.time.LocalDate

class Event6ValidatorSpec extends SpecBase with Matchers with MockitoSugar with BeforeAndAfterEach {
  //scalastyle:off magic.number

  import Event6ValidatorSpec._

  override def beforeEach(): Unit = {
    Mockito.reset(mockFrontendAppConfig)
    when(mockFrontendAppConfig.validEvent6Header).thenReturn(header)
  }

  "Event 6 validator" - {
    "return items in user answers when there are no validation errors" in {
      val validCSVFile = CSVParser.split(
        s"""$header
                            Joe,Bloggs,AA234567D,enhanced lifetime allowance,1234567A,10.00,08/11/2022
                            Steven,Bloggs,AA123456C,fixed protection 2014,1234567A,10.00,12/08/2022"""
      )
      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2023"), nonEventTypeData = true)
      val result = validator.validate(validCSVFile, ua)
      result mustBe Valid(ua
        .setOrException(MembersDetailsPage(Event6, 0).path, Json.toJson(SampleData.memberDetails))
        .setOrException(TypeOfProtectionPage(Event6, 0).path, Json.toJson(TypeOfProtection.EnhancedLifetimeAllowance.toString))
        .setOrException(InputProtectionTypePage(Event6, 0).path, Json.toJson("1234567A"))
        .setOrException(AmountCrystallisedAndDatePage(Event6, 0).path, Json.toJson(SampleData.crystallisedDetails))
        .setOrException(MembersDetailsPage(Event6, 1).path, Json.toJson(SampleData.memberDetails2))
        .setOrException(TypeOfProtectionPage(Event6, 1).path, Json.toJson(TypeOfProtection.FixedProtection2014.toString))
        .setOrException(InputProtectionTypePage(Event6, 1).path, Json.toJson("1234567A"))
        .setOrException(AmountCrystallisedAndDatePage(Event6, 1).path, Json.toJson(SampleData.crystallisedDetails2))
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
      DateHelper.setDate(Some(LocalDate.of(2023, 6, 1)))
      val csvFile = CSVParser.split(
        s"""$header
        Joe,,AA234567D,enhanced lifetime allowance,,12.20,08/11/2022
        Joe,Bloggs,,Enhanced lifetime allowance,12345678,12.20,08/11/2026
        Steven,Bloggs,AA234567D,fixed protection 2014,1234567A,10.00,12/08/2022
        Steven,Bloggs,AA234567D,fixed protection 2014,1234567A,10.00,12/08/2022"""
      )
      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2023"), nonEventTypeData = true)

      val result = validator.validate(csvFile, ua)
      result mustBe Invalid(Seq(
        ValidationError(1, 1, "membersDetails.error.lastName.required", "lastName"),
        ValidationError(1, 4, "inputProtectionType.error.required", "typeOfProtectionReference"),
        ValidationError(2, 2, "membersDetails.error.nino.required", "nino"),
        ValidationError(2, 3, "typeOfProtection.error.format", "typeOfProtection"),
        ValidationError(2, 6, "Date must be between 06 April 2006 and 05 April 2024", "crystallisedDate"),
        ValidationError(4, 2, "membersDetails.error.nino.notUnique", "nino")
      ))
    }
  }

}

object Event6ValidatorSpec {
  private val header = "Members first name,Members last name,Members National Insurance number," +
    "The type of protection held for the crystallisation (see members LTA protection certificate)," +
    "Members protection reference (see members LTA protection certificate)," +
    "Total amount crystallised (£)," +
    "Date of the benefit crystallisation (XX/XX/XXXX)"
  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  private val membersDetailsFormProvider = new MembersDetailsFormProvider
  private val typeOfProtectionFormProvider = new TypeOfProtectionFormProvider
  private val inputProtectionTypeFormProvider = new InputProtectionTypeFormProvider
  private val amountCrystallisedAndDateFormProvider = new AmountCrystallisedAndDateFormProvider

  private val validator = new Event6Validator(membersDetailsFormProvider, typeOfProtectionFormProvider,
    inputProtectionTypeFormProvider, amountCrystallisedAndDateFormProvider, mockFrontendAppConfig)
}
