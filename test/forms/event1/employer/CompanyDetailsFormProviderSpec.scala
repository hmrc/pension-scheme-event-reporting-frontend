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

package forms.event1.employer

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class CompanyDetailsFormProviderSpec extends StringFieldBehaviours with Constraints {

  private val form = new CompanyDetailsFormProvider()()

  private val companyNameLength: Int = 160
  private val companyNumberLength: Int = 8

  ".companyName" - {

    val fieldName = "companyName"
    val requiredKey = "companyDetails.companyName.error.required"
    val lengthKey = "companyDetails.companyName.error.length"
    val invalidKey = "companyDetails.companyName.error.invalidCharacters"
    val maxLength = companyNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexSafeText)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "{invalid}",
      error = FormError(fieldName, invalidKey, Seq(regexSafeText))
    )
  }

  ".companyNumber" - {

    val fieldName = "companyNumber"
    val requiredKey = "companyDetails.companyNumber.error.required"
    val lengthKey = "companyDetails.companyNumber.error.length"
    val invalidKey = "companyDetails.companyNumber.error.invalidCharacters"
    val maxLength = companyNumberLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexCrn)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "AB12$212",
      error = FormError(fieldName, invalidKey, Seq(regexCrn))
    )
  }
}
