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

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class InputProtectionTypeFormProvider @Inject() extends Mappings {

  private val PROTECTION_REF_LENGTH_8: Int = 8
  private val PROTECTION_REF_LENGTH_12: Int = 12
  private val PROTECTION_REF_LENGTH_15: Int = 15

  def apply(protectionType:String): Form[String] =
    Form(
      "value" -> text("inputProtectionType.error.required")
        .verifying(
          firstError(
            maxLength(getMaxLengthForProtectionType(protectionType), getMaxLengthErrorMsg(protectionType)),
            regexp(getRegexForProtectionType(protectionType), getInvalidFormatErrorMsg(protectionType)))
        )
    )

  private def getMaxLengthForProtectionType(protectionType: String): Int = {
    protectionType match {
      case "enhancedLifetimeAllowance" => PROTECTION_REF_LENGTH_12
      case "enhancedProtection" => PROTECTION_REF_LENGTH_8
      case "fixedProtection" => PROTECTION_REF_LENGTH_8
      case "fixedProtection2014" => PROTECTION_REF_LENGTH_15
      case "fixedProtection2016" => PROTECTION_REF_LENGTH_15
      case "individualProtection2014" => PROTECTION_REF_LENGTH_15
      case "individualProtection2016" => PROTECTION_REF_LENGTH_15
    }
  }

  private def getMaxLengthErrorMsg(protectionType: String): String = {
    protectionType match {
      case "enhancedLifetimeAllowance" => "inputProtectionType.enhancedLifetimeAllowance.error.length"
      case "enhancedProtection" => "inputProtectionType.enhancedProtection.error.length"
      case "fixedProtection" => "inputProtectionType.fixedProtection.error.length"
      case "fixedProtection2014" => "inputProtectionType.fixedProtection2014.error.length"
      case "fixedProtection2016" => "inputProtectionType.fixedProtection2016.error.length"
      case "individualProtection2014" => "inputProtectionType.individualProtection2014.error.length"
      case "individualProtection2016" => "inputProtectionType.individualProtection2016.error.length"
    }
  }

  private def getInvalidFormatErrorMsg(protectionType: String): String = {
    protectionType match {
      case "enhancedLifetimeAllowance" => "inputProtectionType.enhancedLifetimeAllowance.error.invalid"
      case "enhancedProtection" => "inputProtectionType.enhancedProtection.error.invalid"
      case "fixedProtection" => "inputProtectionType.fixedProtection.error.invalid"
      case "fixedProtection2014" => "inputProtectionType.fixedProtection2014.error.invalid"
      case "fixedProtection2016" => "inputProtectionType.fixedProtection2016.error.invalid"
      case "individualProtection2014" => "inputProtectionType.individualProtection2014.error.invalid"
      case "individualProtection2016" => "inputProtectionType.individualProtection2016.error.invalid"
    }
  }

  private def getRegexForProtectionType(protectionType :String):String = {
    protectionType match {
      case "enhancedLifetimeAllowance" => enhancedLifetimeAllowance
      case "enhancedProtection" => enhancedProtectionRegex
      case "fixedProtection" => fixedProtectionRegex
      case "fixedProtection2014" => fixedProtection2014Regex
      case "fixedProtection2016" => fixedProtection2016Regex
      case "individualProtection2014" => individualProtection2014Regex
      case "individualProtection2016" => individualProtection2016Regex
    }
  }
}
