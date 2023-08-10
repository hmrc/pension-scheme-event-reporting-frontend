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

package forms

import java.time.LocalDate
import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages

class PlaygroundFormProvider @Inject() extends Mappings {

  import PlaygroundFormProvider._

  def apply()(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> newLocalDate(
        invalidKey                    = "playground.error.invalid",
        threeDateComponentsMissingKey = "playground.error.required.all",
        twoDateComponentsMissingKey   = "playground.error.required.two",
        oneDateComponentMissingKey    = "playground.error.required"
      ).verifying(
        yearHas4Digits("paymentValueAndDate.date.error.outsideDateRanges"),
        minDate(startDate, messages("paymentValueAndDate.date.error.outsideRelevantTaxYear", startDate.getYear.toString, endDate.getYear.toString)),
        maxDate(endDate, messages("paymentValueAndDate.date.error.outsideRelevantTaxYear", startDate.getYear.toString, endDate.getYear.toString))
      )
    )
}

object PlaygroundFormProvider {
  // For testing purposes
  val startDate: LocalDate = LocalDate.of(2023, 4, 6)
  val endDate: LocalDate = LocalDate.of(2024, 4, 5)
}
