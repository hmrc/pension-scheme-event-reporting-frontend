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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages

import java.time.{LocalDate, Month}
import javax.inject.Inject

class DatePaidFormProvider @Inject() extends Mappings {
  def apply(taxYear: Int)(implicit messages: Messages): Form[LocalDate] = {
  // scalastyle:off magic.number
  val startDate: LocalDate = LocalDate.of(2006, Month.APRIL, 6)
  val endDate: LocalDate = LocalDate.of(taxYear + 1, Month.APRIL, 5)
    println(endDate)
    println(endDate)
    println(endDate)
    println(endDate)
    Form(
      "value" -> localDate(
        invalidKey = "datePaid.event2.error.invalid",
        threeDateComponentsMissingKey = "datePaid.event2.error.required.all",
        twoDateComponentsMissingKey = "datePaid.event2.error.required.two",
        oneDateComponentMissingKey = "datePaid.event2.error.required"
      ).verifying(
        yearHas4Digits("datePaid.event2.error.invalid"),
        minDate(startDate, messages("datePaid.event2.error.outside.taxYear", startDate.getYear.toString,(taxYear + 1).toString)),
        maxDate(endDate, messages("datePaid.event2.error.outside.taxYear", startDate.getYear.toString,(taxYear + 1).toString)))
      )
  }
}
