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

package forms.common

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import models.common.MembersDetails
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event23}
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

import scala.util.Random

class MembersDetailsFormProviderSpec extends StringFieldBehaviours with Constraints {

  val listOfEvents: Seq[EventType] = Seq(Event1, Event22, Event23)
  val event: EventType = Random.shuffle(listOfEvents).head
  val form = new MembersDetailsFormProvider()(event)

  ".firstName" - {

    val requiredKey = "membersDetails.error.firstName.required"
    val lengthKey = "membersDetails.error.firstName.length"
    val invalidKey = "membersDetails.error.firstName.invalid"
    val maxLength = MembersDetailsFormProvider.maximumNameLength
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
    val maxLength = MembersDetailsFormProvider.maximumNameLength
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
    val lengthKey = "genericNino.error.invalid.length"
    val prefixKey = "genericNino.error.invalid.prefix"
    val numbersKey = "genericNino.error.invalid.numbers"
    val suffixKey = "genericNino.error.invalid.suffix"
    val invalidKey = "genericNino.error.invalid"
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

    List(("12345678", lengthKey), ("123456789", prefixKey), ("AA34B6789", numbersKey),
      ("AA3456789", suffixKey), ("AA345678E", invalidKey)).foreach { ninoAndError =>
      s"fail to bind when NINO ${ninoAndError._1} is invalid with appropriate error ${ninoAndError._2}" in {
        val result = form.bind(Map("firstName" -> "validFirstName", "lastName" -> "validLastName", "nino" -> ninoAndError._1))
        result.errors mustBe Seq(FormError("nino", ninoAndError._2))
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
