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

package forms.event24

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class TypeOfProtectionGroup1ReferenceFormProviderSpec extends StringFieldBehaviours with Constraints {

  private val requiredKey = "typeOfProtectionReference.error.required"
  private val lengthKey = "typeOfProtectionReference.event24.error.length"
  private val maxLength = 15
  private val minLength = 8

  private val form = new TypeOfProtectionGroup1ReferenceFormProvider()()

  ".value" - {

    val fieldName = "nonResidenceEnhancement"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(protectionReferenceRegex)
    )

    behave like fieldWithMinLength(
      form,
      fieldName,
      minLength = minLength,
      lengthError = FormError(fieldName, lengthKey, Seq(minLength))
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
  }
}
