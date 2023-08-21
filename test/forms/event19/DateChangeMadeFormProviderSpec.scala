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

package forms.event19

import forms.behaviours.DateBehaviours
import play.api.i18n.Messages

import java.time.LocalDate

class DateChangeMadeFormProviderSpec(implicit messages: Messages) extends DateBehaviours {

  private val form = new DateChangeMadeFormProvider()(2022)

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2022, 4, 6),
      max = LocalDate.of(2023, 4, 5)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "event19.dateChangeMade.error.required.all")
  }
}
