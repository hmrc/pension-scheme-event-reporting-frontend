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

package forms.event1.employer


import forms.mappings.{Mappings, Transforms}
import models.event1.employer.LoanDetails
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Inject

class LoanDetailsFormProvider @Inject() extends Mappings with Transforms {

  import LoanDetailsFormProvider._

  def apply(): Form[LoanDetails] =
    Form(
      mapping("loanAmount" ->
        optionBigDecimal2DP(
          "loanDetails.loanAmount.notANumber",
          "loanDetails.loanAmount.tooManyDecimals")
          .verifying(
            maximumValue[BigDecimal](maxPaymentValue, "loanDetails.loanAmount.amountTooHigh"),
            minimumValue[BigDecimal](0, "loanDetails.loanAmount.negative")
          ), "fundValue" ->
        optionBigDecimal2DP(
          "loanDetails.fundValue.notANumber",
          "loanDetails.fundValue.tooManyDecimals")
          .verifying(
            maximumValue[BigDecimal](maxPaymentValue, "loanDetails.fundValue.amountTooHigh"),
            minimumValue[BigDecimal](0, "loanDetails.fundValue.negative")
          )
      )
      (LoanDetails.apply)(LoanDetails.unapply)
    )
}


object LoanDetailsFormProvider {
  val maxPaymentValue: BigDecimal = 999999999.99
}
