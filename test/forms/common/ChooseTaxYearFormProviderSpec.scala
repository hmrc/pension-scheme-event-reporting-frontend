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

import forms.behaviours.OptionFieldBehaviours
import models.common.ChooseTaxYear
import models.enumeration.EventType.{Event22, Event23}
import play.api.data.FormError

class ChooseTaxYearFormProviderSpec extends OptionFieldBehaviours {

  ".value" - {

    "event23" - {
      val form = new ChooseTaxYearFormProvider()(Event23, 2021)
      val fieldName = "value"
      val requiredKeyEvent23 = "chooseTaxYear.event23.error.required"

      behave like optionsField[ChooseTaxYear](
        form,
        fieldName,
        validValues = ChooseTaxYear.valuesForYearRange(2021),
        invalidError = FormError(fieldName, "error.invalid")
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKeyEvent23)
      )
    }

    "event22" - {
      val form = new ChooseTaxYearFormProvider()(Event22, 2021)
      val fieldName = "value"
      val requiredKeyEvent22 = "chooseTaxYear.event22.error.required"

      behave like optionsField[ChooseTaxYear](
        form,
        fieldName,
        validValues = ChooseTaxYear.valuesForYearRange(2021),
        invalidError = FormError(fieldName, "error.invalid")
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKeyEvent22)
      )
    }
  }
}
