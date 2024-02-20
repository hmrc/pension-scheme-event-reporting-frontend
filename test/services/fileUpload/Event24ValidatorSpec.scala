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

package services.fileUpload

import base.SpecBase
import cats.data.Validated.Invalid
import config.FrontendAppConfig
import forms.common.MembersDetailsFormProvider
import forms.event24._
import models.{TaxYear, UserAnswers}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.TaxYearPage
import services.fileUpload.ValidatorErrorMessages.HeaderInvalidOrFileIsEmpty
import utils.DateHelper

import java.time.LocalDate
import scala.collection.immutable.ArraySeq

class Event24ValidatorSpec extends SpecBase with Matchers with MockitoSugar with BeforeAndAfterEach {
  import Event24ValidatorSpec._

  override def beforeEach(): Unit = {
    Mockito.reset(mockFrontendAppConfig)
    when(mockFrontendAppConfig.validEvent24Header).thenReturn(header)
  }

  "Event 24 Validator" - {
//    "return a valid result if there are no validation errors" in {
//      val validCSVFile = CSVParser.split(
//        s"""$header
//                            Jane,Doe,AB123456A,13/11/2023,ANN,123,YES,FIXED,abcdef123,NO,YES,YES,123/ABCDEF
//                            Jane,Doe,AB123456B,13/11/2023,ANN,123,NO,,,YES,,NO,
//                            Jane,Doe,AB123456C,13/11/2023,ANN,123,YES,SPEC,,NO,NO,,"""
//      )
//      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2023"), nonEventTypeData = true)
//      val result = validator.validate(validCSVFile, ua)
//      result.isValid mustBe true
//    }
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
    "return validation errors when tax year out of range" in {
      DateHelper.setDate(Some(LocalDate.of(2023, 6, 1)))
      val csvFile = CSVParser.split(
        s"""$header
                          Jane,Doe,AB123456A,13/11/2026,ANN,123,YES,FIXED,abcdef123,NO,YES,YES,123/ABCDEF"""
      )
      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2023"), nonEventTypeData = true)
      val result = validator.validate(csvFile, ua)
      result mustBe Invalid(Seq(
        ValidationError(1, 3, "Date must be between 06 April 2023 and 05 April 2024", "crystallisedDate"),
      ))
    }
//    "return validation errors if present" in {
//      DateHelper.setDate(Some(LocalDate.of(2023, 6, 1)))
//      val csvFile = CSVParser.split(
//        s"""$header
//                          ,Doe,AB123456A,13/11/2023,ANN,123,YES,FIXED,abcdef123,NO,YES,YES,123/ABCDEF
//                          Jane,,AB123456A,13/11/2023,ANN,123,YES,FIXED,abcdef123,NO,YES,YES,123/ABCDEF
//                          Jane,Doe,,13/11/2023,ANN,123,YES,FIXED,abcdef123,NO,YES,YES,123/ABCDEF
//                          Jane,Doe,AB123456A,,ANN,123,YES,FIXED,abcdef123,NO,YES,YES,123/ABCDEF
//                          Jane,Doe,AB123456B,13/11/202,ANN,123,YES,FIXED,abcdef123,NO,YES,YES,123/ABCDEF
//                          Jane,Doe,AB123456C,13/11/2023,ANNI,123,YES,FIXED,abcdef123,NO,YES,YES,123/ABCDEF
//                          Jane,Doe,AB123456D,13/11/2023,ANN,abc,YES,FIXED,abcdef123,NO,YES,YES,123/ABCDEF
//                          Jane,Doe,AB123457A,13/11/2023,ANN,123,,FIXED,abcdef123,NO,YES,YES,123/ABCDEF
//                          Jane,Doe,AB123457B,13/11/2023,ANN,123,YES,FIXES,abcdef123,NO,YES,YES,123/ABCDEF
//                          Jane,Doe,AB123457C,13/11/2023,ANN,123,YES,FIXED,,NO,YES,YES,123/ABCDEF
//                          Jane,Doe,AB123457D,13/11/2023,ANN,123,YES,FIXED,abcdef123,,YES,YES,123/ABCDEF
//                          Jane,Doe,AB123458A,13/11/2023,ANN,123,YES,FIXED,abcdef123,NO,a,YES,123/ABCDEF
//                          Jane,Doe,AB123458B,13/11/2023,ANN,123,YES,FIXED,abcdef123,NO,YES,YES,123^ABCDEF
//                          Jane,Doe,AB123458C,13/11/2023,ANN,123,YES,FIXED,abcdef123,NO,YES,YES,"""
//      )
//      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2023"), nonEventTypeData = true)
//      val result = validator.validate(csvFile, ua)
//      result mustBe Invalid(Seq(
//        ValidationError(1, 0, "membersDetails.error.firstName.required", "firstName"),
//        ValidationError(2, 1, "membersDetails.error.lastName.required", "lastName"),
//        ValidationError(3, 2, "membersDetails.error.nino.required", "nino"),
//        ValidationError(4, 3, "genericDate.error.invalid.allFieldsMissing", "crystallisedDate"),
//        ValidationError(5, 3, "genericDate.error.invalid.year", "crystallisedDate"),
//        ValidationError(6, 4, "bceTypeSelection.error.format", "bceType"),
//        ValidationError(7, 5, "totalAmountBenefitCrystallisation.event24.error.nonNumeric", "totalAmount"),
//        ValidationError(8, 6, "validProtection.event24.error.required", "validProtection"),
//        ValidationError(9, 7, "typeOfProtection.event24.error.format", "protectionType"),
//        ValidationError(10, 8, "typeOfProtectionReference.error.required", "protectionReference"),
//        ValidationError(11, 9, "overAllowance.event24.error.required", "overAllowance"),
//        ValidationError(12, 10, "error.boolean", "overAllowanceAndDeathBenefit"),
//        ValidationError(13, 12, "employerPayeReference.event24.error.disallowedChars", "employerPayeRef", ArraySeq("[A-Za-z0-9/]{9,12}")),
//        ValidationError(14, 12, "employerPayeReference.event24.error.required", "employerPayeRef")),
//      )
//    }
  }
}

object Event24ValidatorSpec {
  private val header = "First name,Last name,National Insurance number," +
    "When was the relevant benefit crystallisation event? (XX/XX/XXXX)," +
    "What was the type of relevant benefit crystallisation event? (see instructions)," +
    "What was the total of the relevant benefit crystallisation event? (£)," +
    "Does the member hold a valid form of protection or enhancement? (YES/NO)," +
    "IF YES TO G: What type of lifetime allowance protection or enhancement is held? (see instructions)," +
    "What is the member's protection reference? (see instructions)," +
    "Has this lump sum payment taken the member over their available lump sum allowance? (YES/NO)," +
    "IF NO TO J: Has this lump sum payment taken the member over their available lump sum and death benefit allowance? (YES/NO)" +
    ",IF YES TO J or K: Has the excess been taxed at marginal rate for this member?," +
    "IF YES TO L:What is the employer PAYE reference used to report the excess for this member? (see instructions)"

  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  private val membersDetailsFormProvider = new MembersDetailsFormProvider
  private val bceTypeSelectionFormProvider = new BCETypeSelectionFormProvider
  private val crystallisedDateFormProvider = new CrystallisedDateFormProvider
  private val employerPayeReferenceFormProvider = new EmployerPayeReferenceFormProvider
  private val marginalRateFormProvider = new MarginalRateFormProvider
  private val overAllowanceFormProvider = new OverAllowanceFormProvider
  private val overAllowanceAndDeathBenefitFormProvider = new OverAllowanceAndDeathBenefitFormProvider
  private val totalAmountBenefitCrystallisationFormProvider = new TotalAmountBenefitCrystallisationFormProvider
  private val typeOfProtectionGroup1FormProvider = new TypeOfProtectionGroup1FormProvider
  private val typeOfProtectionGroup1ReferenceFormProvider = new TypeOfProtectionGroup1ReferenceFormProvider
  private val typeOfProtectionGroup2FormProvider = new TypeOfProtectionGroup2FormProvider
  private val typeOfProtectionGroup2ReferenceFormProvider = new TypeOfProtectionGroup2ReferenceFormProvider
  private val validProtectionFormProvider = new ValidProtectionFormProvider

  private val validator = new Event24Validator(
    membersDetailsFormProvider,
    bceTypeSelectionFormProvider,
    crystallisedDateFormProvider,
    employerPayeReferenceFormProvider,
    marginalRateFormProvider,
    overAllowanceFormProvider,
    overAllowanceAndDeathBenefitFormProvider,
    totalAmountBenefitCrystallisationFormProvider,
    typeOfProtectionGroup1FormProvider,
    typeOfProtectionGroup1ReferenceFormProvider,
    typeOfProtectionGroup2FormProvider,
    typeOfProtectionGroup2ReferenceFormProvider,
    validProtectionFormProvider,
    mockFrontendAppConfig
  )
}
