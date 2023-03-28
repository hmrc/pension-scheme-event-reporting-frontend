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

import javax.inject.Inject

class AmountPaidFormProvider @Inject() extends Mappings {

import forms.event2.AmountPaidFormProvider._

  def apply(): Form[BigDecimal] =
    Form(
      "value" ->
        bigDecimal2DP(
        "amountPaid.event2.error.nothingEntered",
        "amountPaid.event2.error.nonNumeric",
        "amountPaid.event2.error.noDecimals")
        .verifying(
          maximumValue[BigDecimal](maxAmountPaidValue, "amountPaid.event2.error.tooHigh"),
          minimumValue[BigDecimal](0, "amountPaid.event2.error.negative"),
          zeroValue[BigDecimal](0.01, "amountPaid.event2.error.zeroEntered")
        ))
}

object AmountPaidFormProvider {
  private val maxAmountPaidValue: BigDecimal = 999999999.99
}