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

package forms.event24

import forms.behaviours.OptionFieldBehaviours
import models.event24.BCETypeSelection
import play.api.data.FormError

class BCETypeSelectionFormProviderSpec extends OptionFieldBehaviours {

  private val form = new BCETypeSelectionFormProvider()()

  ".value" - {

    val fieldName = "value"
    val formatKey = "bceTypeSelection.event24.error.format"
    val requiredKey = "bceTypeSelection.event24.error.required"

    behave like optionsField[BCETypeSelection](
      form,
      fieldName,
      validValues  = BCETypeSelection.values,
      invalidError = FormError(fieldName, formatKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
