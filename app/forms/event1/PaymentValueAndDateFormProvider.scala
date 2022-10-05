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


import forms.mappings.Mappings
import models.event1.PaymentDetails

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms.mapping

class PaymentValueAndDateFormProvider @Inject() extends Mappings {

  import forms.event1.PaymentValueAndDateFormProvider._

  def apply(): Form[PaymentDetails] =
    Form(
      mapping("paymentValue" ->
        double("paymentValueAndDate.value.error.nothingEntered",
          "paymentValueAndDate.value.error.notANumber",
          "paymentValueAndDate.error.noDecimals",
          "paymentValueAndDate.error.amountTooHigh")
          .verifying(
            firstError(
              // Nothing entered
              // maximumValue[Double](maxPaymentValue,"paymentValueAndDate.error.amountTooHigh"),
              // Not a number
              // maximumValue[Double](maxPaymentValue,"paymentValueAndDate.error.amountTooHigh"),
              // No decimals
              // maximumValue[Double](maxPaymentValue,"paymentValueAndDate.error.amountTooHigh"),
              // Amount too high
              maximumValue[Double](maxPaymentValue,"paymentValueAndDate.error.amountTooHigh")
            )
        ))(PaymentDetails.apply)(PaymentDetails.unapply)
}

object PaymentValueAndDateFormProvider {
  val maxPaymentValue: Double = 999999999.99
}
