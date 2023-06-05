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

import java.time.LocalDate

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class DateChangeMadeFormProvider @Inject() extends Mappings {

  def apply(taxYear: Int): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey                    = "event19.dateChangeMade.error.invalid",
        threeDateComponentsMissingKey = "event19.dateChangeMade.error.required.all",
        twoDateComponentsMissingKey   = "event19.dateChangeMade.error.required.two",
        oneDateComponentMissingKey    = "event19.dateChangeMade.error.required"
      ).verifying(
        minDate(LocalDate.of(taxYear, 4,6), "event19.dateChangeMade.error.outside.taxYear", taxYear.toString, (taxYear + 1).toString),
        maxDate(LocalDate.of(taxYear + 1, 4,5), "event19.dateChangeMade.error.outside.taxYear", taxYear.toString, (taxYear + 1).toString)
      )
    )
}
