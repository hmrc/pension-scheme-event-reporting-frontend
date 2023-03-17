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

package forms.event6

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen
import forms.mappings.Constraints

class InputProtectionTypeFormProviderSpec extends StringFieldBehaviours with Constraints {

  private val minLength: Int = 8
  private val maxLength: Int = 15

  "InputProtectionTypeFormProviderSpec for protection type" - {

    testInputProtectionTypeFormProvider("enhancedLifetimeAllowance")
    testInputProtectionTypeFormProvider("enhancedProtection")
    testInputProtectionTypeFormProvider("fixedProtection")
    testInputProtectionTypeFormProvider("fixedProtection2014")
    testInputProtectionTypeFormProvider("fixedProtection2016")
    testInputProtectionTypeFormProvider("individualProtection2014")
    testInputProtectionTypeFormProvider("individualProtection2016")

  }

  private def testInputProtectionTypeFormProvider(protectionType: String): Unit = {

    s".value for protectionType $protectionType" - {

      val fieldName = "value"
      val lengthKey = "inputProtectionType.error.length"
      val invalidKey = "inputProtectionType.error.invalid"
      val requiredKey = "inputProtectionType.error.required"

      val form = new InputProtectionTypeFormProvider().apply()

      //valid data format test
      behave like fieldThatBindsValidData(
        form,
        fieldName,
        RegexpGen.from(inputProtectionTypeRegex)
      )

      //invalid data format test
      behave like fieldWithRegex(
        form,
        fieldName,
        invalidString = "A#B444-A",
        error = FormError(fieldName, invalidKey, Seq(inputProtectionTypeRegex))
      )

      //max length test
      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = maxLength,
        lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
      )

      //min length test
      behave like fieldWithMinLength(
        form,
        fieldName,
        minLength = minLength,
        lengthError = FormError(fieldName, lengthKey, Seq(minLength))
      )

      //mandatory field test
      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }
  }
}
