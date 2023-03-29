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

package forms.event2

import java.time.LocalDate
import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class datePaidFormProvider @Inject() extends Mappings {

  def apply(taxYear: String): Form[LocalDate] = {
    Form(
      "value" -> localDate(
        invalidKey                    = "datePaid.event2.error.nonNumeric",
        threeDateComponentsMissingKey = "datePaid.event2.error.noDayMonthOrYear",
        twoDateComponentsMissingKey   = "datePaid.event2.error.noDayMonthOrYear",
        oneDateComponentMissingKey    = "datePaid.event2.error.noDayMonthOrYear"
      ).verifying(
        yearHas4Digits("datePaid.event2.error.nonNumeric"),
        minDate(LocalDate.of(taxYear.toInt, 4, 6), "datePaid.event2.error.outside.taxYear", taxYear, (taxYear.toInt + 1).toString),
        maxDate(LocalDate.of(taxYear.toInt + 1, 4, 5), "datePaid.event2.error.outside.taxYear",taxYear, (taxYear.toInt + 1).toString)
      )
    )
  }
}
