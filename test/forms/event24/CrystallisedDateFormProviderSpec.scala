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

package forms.event24

import base.SpecBase
import forms.behaviours.{BigDecimalFieldBehaviours, DateBehavioursTrait}
import play.api.data.FormError
import utils.DateHelper.formatDateDMY

import java.time.LocalDate

class CrystallisedDateFormProviderSpec extends SpecBase
  with BigDecimalFieldBehaviours with DateBehavioursTrait {

  private val stubMax: LocalDate = LocalDate.of(2025, 4, 5)
  private val stubMin: LocalDate = LocalDate.of(2024, 4, 6)
  private val form = new CrystallisedDateFormProvider()(stubMin, stubMax)

  ".crystallisedDate" - {
    behave like dateFieldYearNot4Digits(
      form = form,
      key = "crystallisedDate",
      formError = FormError("crystallisedDate", "genericDate.error.invalid.year")
    )

    behave like dateFieldWithMax(
      form = form,
      key = "crystallisedDate",
      max = stubMax,
      formError = FormError(
        "crystallisedDate",
        messages("genericDate.error.outsideReportedYear",
          formatDateDMY(stubMin),
          formatDateDMY(stubMax)
        )
      )
    )

    behave like mandatoryDateField(
      form,
      "crystallisedDate",
      "genericDate.error.invalid.allFieldsMissing"
    )
  }
}
