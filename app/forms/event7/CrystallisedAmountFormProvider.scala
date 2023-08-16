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

package forms.event7

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class CrystallisedAmountFormProvider @Inject() extends Mappings {

  import forms.event7.CrystallisedAmountFormProvider.maxCrystallisedAmount

  def apply(): Form[BigDecimal] =
    Form(
      "crystallisedAmount" -> bigDecimal2DP(
        "crystallisedAmount.value.error.nothingEntered",
        "amounts.value.error.notANumber",
        "amounts.value.error.tooManyDecimals")
        .verifying(
          maximumValue[BigDecimal](maxCrystallisedAmount, "amounts.value.error.amountTooHigh"),
          minimumValue[BigDecimal](0, "amounts.value.error.negative"),
          minimumValue[BigDecimal](0.01, "amounts.value.error.zeroAmount")
        )
    )
}

object CrystallisedAmountFormProvider {
  val maxCrystallisedAmount: BigDecimal = 999999999.99
}
