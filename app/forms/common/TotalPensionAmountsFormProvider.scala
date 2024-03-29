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

package forms.common

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class TotalPensionAmountsFormProvider @Inject() extends Mappings {

  import forms.common.TotalPensionAmountsFormProvider._

  def apply(): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal2DP(
        "totalPensionAmounts.value.error.nothingEntered",
        "totalPensionAmounts.value.error.notANumber",
        "totalPensionAmounts.value.error.tooManyDecimals")
        .verifying(
          maximumValue[BigDecimal](maxPensionAmtValue, "totalPensionAmounts.value.error.amountTooHigh"),
          minimumValue[BigDecimal](0, "totalPensionAmounts.value.error.negative"),
          minimumValue[BigDecimal](0.01, "totalPensionAmounts.value.error.zeroAmount")
        )
    )
}

object TotalPensionAmountsFormProvider {
  val maxPensionAmtValue: BigDecimal = 999999999.99
}
