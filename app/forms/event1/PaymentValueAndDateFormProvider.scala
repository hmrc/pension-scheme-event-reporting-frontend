/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms.mapping
import utils.DateHelper.formatDateDMY
import play.api.i18n.Messages

import java.time.LocalDate

class PaymentValueAndDateFormProvider @Inject() extends Mappings with Transforms {

  import forms.event1.PaymentValueAndDateFormProvider._

  def apply(min: LocalDate, max: LocalDate)(implicit messages: Messages): Form[PaymentDetails] =
    Form(
      mapping("paymentValue" ->
        bigDecimal2DP("paymentValueAndDate.value.error.nothingEntered",
          "paymentValueAndDate.value.error.notANumber",
          "paymentValueAndDate.value.error.noDecimals")
          .verifying(
            maximumValue[BigDecimal](maxPaymentValue, "paymentValueAndDate.value.error.amountTooHigh")
          ), "paymentDate" ->
              localDate(
                  requiredKey = "paymentValueAndDate.date.error.nothingEntered",
                  invalidKey = "paymentValueAndDate.date.error.outsideRelevantTaxYear",
                  allRequiredKey = "paymentValueAndDate.date.error.noDayMonthOrYear",
                  twoRequiredKey = "paymentValueAndDate.date.error.outsideDateRanges"
              ).verifying(
                /*
                  paymentValueAndDate.date.error.nothingEntered = Enter the date of payment or when benefit made available
                  paymentValueAndDate.date.error.outsideDateRanges = Enter a real date
                */
                minDate(min, messages("paymentValueAndDate.date.error.outsideRelevantTaxYear", formatDateDMY(min), formatDateDMY(max))),
                maxDate(max, messages("paymentValueAndDate.date.error.outsideRelevantTaxYear", formatDateDMY(min), formatDateDMY(max))),
                yearHas4Digits("paymentValueAndDate.date.error.noDayMonthOrYear")
              )
          )
      (PaymentDetails.apply)(PaymentDetails.unapply)
    )
}


object PaymentValueAndDateFormProvider {
  val maxPaymentValue: BigDecimal = 999999999.99
}
