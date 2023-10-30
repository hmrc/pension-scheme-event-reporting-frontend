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

package forms.event25

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class EmployerPayeReferenceFormProviderSpec extends StringFieldBehaviours with Constraints {

  private val lengthErrorKey = "employerPayeReference.event25.error.length"
  private val requiredErrorKey = "employerPayeReference.event25.error.required"
  private val maxLength = 12
  private val minLength = 9

  private val form = new EmployerPayeReferenceFormProvider()()

  ".value" - {

    val fieldName = "value"

    // TODO - fix test - doesn't allow for multiple constraints?
//    behave like fieldThatBindsValidData(
//      form,
//      fieldName,
//      RegexpGen.from(employerIdRefNoSlashRegex)
//    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthErrorKey, Seq(maxLength))
    )

    behave like fieldWithMinLength(
      form,
      fieldName,
      minLength = minLength,
      lengthError = FormError(fieldName, lengthErrorKey, Seq(minLength))
    )


    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredErrorKey)
    )
  }
}
