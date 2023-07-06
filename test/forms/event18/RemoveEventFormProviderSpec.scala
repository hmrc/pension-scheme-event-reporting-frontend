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

package forms.event18

import base.SpecBase
import forms.behaviours.BooleanFieldBehaviours
import forms.common.RemoveEventFormProvider
import models.enumeration.EventType.Event18
import play.api.data.FormError

class RemoveEventFormProviderSpec extends BooleanFieldBehaviours with SpecBase {

  private val requiredKey = "removeEvent.error.required"
  private val invalidKey = "error.boolean"

  val form = new RemoveEventFormProvider().apply(Event18)

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
