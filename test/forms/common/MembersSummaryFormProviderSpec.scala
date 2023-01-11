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

package forms.common

import forms.behaviours.BooleanFieldBehaviours
import models.enumeration.EventType.{Event22, Event23}
import play.api.data.FormError

class MembersSummaryFormProviderSpec extends BooleanFieldBehaviours {
  val invalidKey = "error.boolean"
  ".value" - {

    "event22" - {
      val fieldName = "value"
      val requiredKeyEvent22 = "membersSummary.event22.error.required"
      val form = new MembersSummaryFormProvider()(Event22)

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKeyEvent22)
      )
    }

    "event23" - {
      val fieldName = "value"
      val requiredKeyEvent23 = "membersSummary.event23.error.required"
      val form = new MembersSummaryFormProvider()(Event23)

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKeyEvent23)
      )
    }
  }
}
