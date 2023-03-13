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

  private val requiredKey = "inputProtectionType.error.required"
  private val PROTECTION_REF_LENGTH_8: Int = 8
  private val PROTECTION_REF_LENGTH_12: Int = 12
  private val PROTECTION_REF_LENGTH_15: Int = 15

  "InputProtectionTypeFormProviderSpec for protection type" - {
    methodTest("enhancedLifetimeAllowance", PROTECTION_REF_LENGTH_12, enhancedLifetimeAllowance, "ABC123654XY")
    methodTest("enhancedProtection", PROTECTION_REF_LENGTH_8, enhancedProtectionRegex, "1234567")
    methodTest("fixedProtection", PROTECTION_REF_LENGTH_8, fixedProtectionRegex, "8111111")
    methodTest("fixedProtection2014", PROTECTION_REF_LENGTH_15, fixedProtection2014Regex, "IP149999999999X")
    methodTest("fixedProtection2014", PROTECTION_REF_LENGTH_15, fixedProtection2014Regex, "1234567")
    methodTest("fixedProtection2016", PROTECTION_REF_LENGTH_15, fixedProtection2016Regex, "FP160000000000")
    methodTest("individualProtection2014", PROTECTION_REF_LENGTH_15, individualProtection2014Regex, "IP149999999999")
    methodTest("individualProtection2014", PROTECTION_REF_LENGTH_15, individualProtection2014Regex, "A999999")
    methodTest("individualProtection2016", PROTECTION_REF_LENGTH_15, individualProtection2016Regex, "IP162222222222")
  }

  private def methodTest(protectionType: String, maxLength: Int, regexProtectionType: String, invalidVal: String): Unit = {

    s".value for protectionType is $protectionType and value is $invalidVal" - {

      val fieldName = "value"
      val lengthKey = s"inputProtectionType.$protectionType.error.length"
      val invalidKey = s"inputProtectionType.$protectionType.error.invalid"
      val form = new InputProtectionTypeFormProvider().apply(protectionType)

      //valid data format test
      behave like fieldThatBindsValidData(
        form,
        fieldName,
        RegexpGen.from(regexProtectionType)
      )

      //invalid data format test
      behave like fieldWithRegex(
        form,
        fieldName,
        invalidString = invalidVal,
        error = FormError(fieldName, invalidKey, Seq(regexProtectionType))
      )

      //max length test
      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = maxLength,
        lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
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
