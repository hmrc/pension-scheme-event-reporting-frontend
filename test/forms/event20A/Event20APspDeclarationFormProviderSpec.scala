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

package forms.event20A

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class Event20APspDeclarationFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "event20A.pspDeclaration.error.required"
  private val lengthKey = "event20A.pspDeclaration.error.length"
  private val maxLength = 8

  val invalidKey = "event20A.pspDeclaration.error.invalid"
  val noMatchKey = "event20A.pspDeclaration.error.noMatch"
  private val psaIdRegex = "^A[0-9]{7}$"

  private val authorisingPsaId = Some("A1234567")

  private val form = new Event20APspDeclarationFormProvider()(authorisingPsaId)
  val fieldName = "value"

  ".value" - {

    "bind valid data" in {
      val validValue = "A1234567"
      val result = form.bind(Map(fieldName -> validValue)).apply(fieldName)
      result.value.value mustBe validValue
    }

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
      invalidString = "11111111",
      error = FormError(fieldName, invalidKey, Seq(psaIdRegex))
    )
  }
}
