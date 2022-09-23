/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.mappings.Constraints
import models.event1.MembersDetails
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class MembersDetailsFormProviderSpec extends StringFieldBehaviours with Constraints {

  val form = new MembersDetailsFormProvider()()

  ".firstName" - {

    val requiredKey = "membersDetails.error.firstName.required"
    val lengthKey = "membersDetails.error.firstName.length"
    val invalidKey = "membersDetails.error.firstName.invalid"
    val maxLength = PersonNameFormProvider.firstNameLength
    val fieldName = "firstName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexName)
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
      invalidString = "1A",
      error = FormError(fieldName, invalidKey, Seq(regexName))
    )
  }

  ".lastName" - {

    val requiredKey = "membersDetails.error.lastName.required"
    val lengthKey = "membersDetails.error.lastName.length"
    val invalidKey = "membersDetails.error.lastName.invalid"
    val maxLength = PersonNameFormProvider.lastNameLength
    val fieldName = "lastName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexName)
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
      invalidString = "1A",
      error = FormError(fieldName, invalidKey, Seq(regexName))
    )
  }

  ".nino" - {

    val requiredKey = "membersDetails.error.nino.required"
    val invalidKey = "membersDetails.error.nino.invalid"
    val fieldName = "nino"

    Seq("aB020202A", "Ab020202A", "AB020202a", "AB020202A", "ab020202a").foreach { nino =>
      s"successfully bind when valid NINO $nino is provided" in {
        val res = form.bind(Map("firstName" -> "validFirstName", "lastName" -> "validLastName", "nino" -> nino))
        res.get mustEqual MembersDetails("validFirstName", "validLastName", "AB020202A")
      }
    }

    "successfully bind when yes is selected and valid NINO with spaces is provided" in {
      val res = form.bind(Map("firstName" -> "validFirstName", "lastName" -> "validLastName", "nino" -> " a b 0 2 0 2 0 2 a "))
      res.get.nino mustEqual "AB020202A"
    }

    Seq("DE999999A", "AO111111B", "ORA12345C", "AB0202020", "AB0303030D", "AB040404E").foreach { nino =>
      s"fail to bind when NINO $nino is invalid" in {
        val result = form.bind(Map("firstName" -> "validFirstName", "lastName" -> "validLastName", "nino" -> nino))
        result.errors mustBe Seq(FormError("nino", invalidKey))
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
