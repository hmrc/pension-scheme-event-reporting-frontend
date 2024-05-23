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

package forms.event1

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import utils.DateConstraintHandlers.regexEvent1Description
import wolfendale.scalacheck.regexp.RegexpGen

class MemberTangibleMoveablePropertyFormProviderSpec extends StringFieldBehaviours {

  private val lengthKey = "memberTangibleMoveableProperty.error.length"
  private val maxLength = 160

  private val form = new MemberTangibleMoveablePropertyFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexEvent1Description)
    )

    s"not bind strings longer than $maxLength characters" in {
      forAll(stringsLongerThan(maxLength) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
      }
    }

    "not bind empty data" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.value.value mustBe ""
      result.errors mustBe List(FormError("value", List("memberTangibleMoveableProperty.error.required"), List()))
    }
  }
}
