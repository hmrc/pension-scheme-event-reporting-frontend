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

package forms.event13

import forms.behaviours.DateBehaviours

import java.time.LocalDate

class ChangeDateFormProviderSpec extends DateBehaviours {

  private val form = new ChangeDateFormProvider()(2000)

  ".value" - {


    val validData = datesBetween(
      min = LocalDate.of(2000, 4, 6),
      max = LocalDate.of(2001, 4, 5),
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "changeDate.error.required.all")
  }
}
