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

package forms.event13

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import utils.DateConstraintHandlers.regexSafeText
import wolfendale.scalacheck.regexp.RegexpGen

class SchemeStructureDescriptionFormProviderSpec extends StringFieldBehaviours {

  private val lengthKey = "event13.schemeStructureDescription.error.length"
  val invalidKey = "event13.schemeStructureDescription.error.invalidCharacters"
  private val maxLength = 150

  private val form = new SchemeStructureDescriptionFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexSafeText).map(_.take(maxLength))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      invalidString = "玚틸훣ȹ",
      error = FormError(fieldName, invalidKey, Seq(regexSafeText))
    )

  }
}
