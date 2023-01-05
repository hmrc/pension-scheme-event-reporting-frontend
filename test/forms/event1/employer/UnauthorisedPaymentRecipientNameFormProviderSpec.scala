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

package forms.event1.employer

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class UnauthorisedPaymentRecipientNameFormProviderSpec extends StringFieldBehaviours with Constraints {

  private val invalidKey = "unauthorisedPaymentRecipientName.employer.error.invalid"
  private val lengthKey = "unauthorisedPaymentRecipientName.employer.error.length"
  private val maxLength = 160

  private val form = new UnauthorisedPaymentRecipientNameFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexPersonOrOrgName)
    )

    "return errors when invalid data" in {
      val dataItem = "-*%$£"
      val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
      result.errors.headOption.map(_.message) mustBe Some(invalidKey)
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    "bind empty data" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.value.value mustBe ""
      result.errors mustBe empty
    }
  }
}
