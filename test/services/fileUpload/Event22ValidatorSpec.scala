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
import services.fileUpload.ValidatorErrorMessages.HeaderInvalidOrFileIsEmpty

class Event22ValidatorSpec extends SpecBase with Matchers with MockitoSugar with BeforeAndAfterEach {
  //scalastyle:off magic.number

  import Event22ValidatorSpec._

  override def beforeEach(): Unit = {
    Mockito.reset(mockFrontendAppConfig)
    when(mockFrontendAppConfig.validEvent22Header).thenReturn(header)
  }

  "Event 22 validator" - {
    "return items in user answers when there are no validation errors" in {
      val validCSVFile = CSVParser.split(
        s"""$header
                            Joe,Bloggs,AA234567D,2020,12.20
                            Steven,Bloggs,AA123456C,2022,13.20"""
      )
      val result = validator.parse(validCSVFile, UserAnswers())
      result mustBe Valid(UserAnswers()
        .setOrException(MembersDetailsPage(Event22, 0).path, Json.toJson(SampleData.memberDetails))
        .setOrException(ChooseTaxYearPage(Event22, 0).path, Json.toJson(ChooseTaxYear("2020")))
        .setOrException(TotalPensionAmountsPage(Event22, 0).path, Json.toJson(BigDecimal(12.20)))
        .setOrException(MembersDetailsPage(Event22, 1).path, Json.toJson(SampleData.memberDetails2))
        .setOrException(ChooseTaxYearPage(Event22, 1).path, Json.toJson(ChooseTaxYear("2022")))
        .setOrException(TotalPensionAmountsPage(Event22, 1).path, Json.toJson(BigDecimal(13.20)))
      )
    }

    "return validation error for incorrect header" in {
      val csvFile = CSVParser.split("""test""")
      val result = validator.parse(csvFile, UserAnswers())
      result mustBe Invalid(Seq(
        ValidationError(0, 0, HeaderInvalidOrFileIsEmpty)
      ))
    }

    "return validation error for empty file" in {
      val result = validator.parse(Nil, UserAnswers())
      result mustBe Invalid(Seq(
        ValidationError(0, 0, HeaderInvalidOrFileIsEmpty)
      ))
    }

    "return validation errors when present" in {
      val csvFile = CSVParser.split(
        s"""$header
,Bloggs,AA234567D,2020,12.20
Steven,,xyz,,"""
      )

      val result = validator.parse(csvFile, UserAnswers())
      result mustBe Invalid(Seq(
        ValidationError(1, 0, "membersDetails.error.firstName.required", "firstName"),
        ValidationError(2, 1, "membersDetails.error.lastName.required", "lastName"),
        ValidationError(2, 2, "membersDetails.error.nino.invalid", "nino"),
        ValidationError(2, 3, "chooseTaxYear.event22.error.required", "taxYear"),
        ValidationError(2, 4, "totalPensionAmounts.value.error.nothingEntered", "totalAmounts")
      ))
    }

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
