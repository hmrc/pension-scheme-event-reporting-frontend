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

package forms.event25

import forms.event25.TotalAmountBenefitCrystallisationFormProvider.maxCrystallisedValue
import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form

class TotalAmountBenefitCrystallisationFormProvider @Inject() extends Mappings {

  def apply(): Form[BigDecimal] =
    Form(
      "value" -> bigDecimal2DP("totalAmountBenefitCrystallisation.event25.error.required",
        "totalAmountBenefitCrystallisation.event25.error.nonNumeric",
        "totalAmountBenefitCrystallisation.event25.error.nonNumeric")
          .verifying(
            minimumValue[BigDecimal](0, "totalAmountBenefitCrystallisation.event25.error.negativeValue"),
            maximumValue[BigDecimal](maxCrystallisedValue, "totalAmountBenefitCrystallisation.event25.error.amountTooHigh")
          )

    )
}

object TotalAmountBenefitCrystallisationFormProvider {
  private val maxCrystallisedValue: BigDecimal = 999999999.99
}
