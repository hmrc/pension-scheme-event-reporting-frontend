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

package forms.event1.member

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SchemeDetailsFormProviderSpec extends StringFieldBehaviours {

  private val validData = "abc"
  private val maxLength = 150
  private val nameLengthErrorKey = "schemeDetails.error.name.length"
  private val refLengthErrorKey = "schemeDetails.error.ref.length"

  private val form = new SchemeDetailsFormProvider()()

  ".schemeName" - {

    val fieldName = "schemeName"

    "bind non-empty data" in {
      val result = form.bind(Map(fieldName -> validData)).apply(fieldName)
      result.value.value mustBe validData
      result.errors mustBe empty
    }

    "bind empty data" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.value.value mustBe ""
      result.errors mustBe empty
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, nameLengthErrorKey, Seq(maxLength))
    )
  }

  ".reference" - {

    val fieldName = "reference"

    "bind non-empty data" in {
      val result = form.bind(Map(fieldName -> validData)).apply(fieldName)
      result.value.value mustBe validData
      result.errors mustBe empty
    }

    "bind empty data" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.value.value mustBe ""
      result.errors mustBe empty
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, refLengthErrorKey, Seq(maxLength))
    )
  }
}
