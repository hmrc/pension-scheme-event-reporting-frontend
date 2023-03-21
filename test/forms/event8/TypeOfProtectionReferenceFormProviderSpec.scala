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

package forms.event8

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class TypeOfProtectionReferenceFormProviderSpec extends StringFieldBehaviours with Constraints {

  private val minLength: Int = 8
  private val maxLength: Int = 15

  private val requiredKey = "typeOfProtectionReference.error.required"
  private val lengthKey = "typeOfProtectionReference.error.length"

  "TypeOfProtectionReferenceFormProviderSpec for protection type" - {
    testInputProtectionTypeFormProvider("primaryProtection")
    testInputProtectionTypeFormProvider("enhancedProtection")
  }

  private def testInputProtectionTypeFormProvider(protectionType: String): Unit = {

    s".value for protectionType $protectionType" - {

      val fieldName = "value"
      val lengthKey = "typeOfProtectionReference.error.length"
      val invalidKey = "typeOfProtectionReference.error.invalid"
      val requiredKey = "typeOfProtectionReference.error.required"

      val form = new TypeOfProtectionReferenceFormProvider().apply()

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