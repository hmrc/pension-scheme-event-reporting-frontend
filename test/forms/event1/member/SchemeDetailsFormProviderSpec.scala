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

package forms.event1.member

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SchemeDetailsFormProviderSpec extends StringFieldBehaviours {

  private val validData = "abc"
  private val maxLength = 160
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

    s"not bind strings longer than $maxLength characters" in {
      forAll(stringsLongerThan(maxLength) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain(FormError(fieldName, nameLengthErrorKey, Seq(maxLength)))
      }
    }

    s"not bind strings longer than $maxLength characters and contains invalid characters" in {
      forAll(stringsWithSpecialChars(maxLength) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain(FormError(fieldName, "description.error.invalid"))
      }
    }
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

    s"not bind strings longer than $maxLength characters" in {
      forAll(stringsLongerThan(maxLength) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain(FormError(fieldName, refLengthErrorKey, Seq(maxLength)))
      }
    }

    s"not bind strings longer than $maxLength characters and contains invalid characters" in {
      forAll(stringsWithSpecialChars(maxLength) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain(FormError(fieldName, "description.error.invalid"))
      }
    }
  }
}
