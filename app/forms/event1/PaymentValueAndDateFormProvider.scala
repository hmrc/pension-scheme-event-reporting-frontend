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

package forms.event1


import forms.mappings.{Mappings, Transforms}
import models.event1.PaymentDetails
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages

import java.time.LocalDate
import javax.inject.Inject

class PaymentValueAndDateFormProvider @Inject() extends Mappings with Transforms {

  import forms.event1.PaymentValueAndDateFormProvider._

  def apply(taxYear: Int)(implicit messages: Messages): Form[PaymentDetails] = {
    val startDate: LocalDate = LocalDate.of(taxYear, 4, 6)
    val endDate: LocalDate = LocalDate.of(taxYear + 1, 4, 5)
    Form(
      mapping("paymentValue" ->
        bigDecimal2DP("paymentValueAndDate.value.error.nothingEntered",
          "paymentValueAndDate.value.error.notANumber",
          "paymentValueAndDate.value.error.tooManyDecimals")
          .verifying(
            maximumValue[BigDecimal](maxPaymentValue, "paymentValueAndDate.value.error.amountTooHigh"),
            minimumValue[BigDecimal](0, "paymentValueAndDate.value.error.negative")
          ), "paymentDate" ->
        localDate(
          invalidKey = "genericDate.error.invalid"
        ).verifying(
          yearHas4Digits("genericDate.error.invalid"),
          minDate(startDate, messages("paymentValueAndDate.date.error.outsideRelevantTaxYear", startDate.getYear.toString, endDate.getYear.toString)),
          maxDate(endDate, messages("paymentValueAndDate.date.error.outsideRelevantTaxYear", startDate.getYear.toString, endDate.getYear.toString))
        )
      )
      (PaymentDetails.apply)(PaymentDetails.unapply)
    )
  }
}

object PaymentValueAndDateFormProvider {

  val maxPaymentValue: BigDecimal = 999999999.99
}
