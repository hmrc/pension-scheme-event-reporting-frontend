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

package forms.event1.employer

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class LoanDetailsFormProviderSpec extends StringFieldBehaviours {
  private val form = new LoanDetailsFormProvider()()

  private val loanAmountKey = "loanAmount"
  private val fundValueKey = "fundValue"

  private val loanAmountNotANumberErrorKey = "loanDetails.loanAmount.notANumber"
//  private val loanAmountNoDecimalsKey = "loanDetails.loanAmount.noDecimals"
  private val loanAmountAmountTooHighErrorKey = "loanDetails.loanAmount.amountTooHigh"

  private def details(loanAmount: String = "12",
                      fundValue: String = "1.00"
                      ): Map[String, String] =
    Map(
      loanAmountKey -> loanAmount,
      fundValueKey -> fundValue
    )

  "loanAmount" - {

    "not bind non-numeric numbers" in {
      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric: String =>
          val result = form.bind(details(loanAmount = nonNumeric))
          result.errors mustEqual Seq(FormError(loanAmountKey, loanAmountNotANumberErrorKey))
      }
    }

    "not bind decimals that are greater than 2 dp" in {
          forAll(decimals -> "decimal") {
            decimal: String =>
              val result = form.bind(details(loanAmount = decimal))
              result.errors mustEqual Seq(FormError(loanAmountKey, loanAmountAmountTooHighErrorKey))
          }
        }
//
//    "not bind decimals below 0.00" in {
//      forAll(decimalsBelowValue(BigDecimal("0.00")) -> "decimalBelowMin") {
//        decimal: String =>
//          val result = form.bind(details(loanAmount = decimal))
//          result.errors.head.key mustEqual totalAmtOfTaxDueAtHigherRateKey
//          result.errors.head.message mustEqual messages(s"$messageKeyAmountTaxDueHigherRateKey.error.minimum", "0.01")
//      }
//    }
//
//    "not bind decimals longer than 11 characters" in {
//      forAll(longDecimalString(12) -> "decimalAboveMax") {
//        decimal: String =>
//          val result = form.bind(details(loanAmount = decimal))
//          result.errors.head.key mustEqual totalAmtOfTaxDueAtHigherRateKey
//          result.errors.head.message mustEqual s"$messageKeyAmountTaxDueHigherRateKey.error.maximum"
//      }
//    }
//
//    "bind 0.00 when positive value bound to totalAmtOfTaxDueAtLowerRate" in {
//      val result = form.bind(details(loanAmount = "0.00"))
//      result.errors mustBe Seq.empty
//    }
//
//    "bind integers to totalAmtOfTaxDueAtHigherRate" in {
//      forAll(intsAboveValue(0) -> "intAboveMax") {
//        i: Int =>
//          val result = form.bind(details(loanAmount = i.toString))
//          result.errors mustBe Seq.empty
//          result.value.flatMap(_.totalAmtOfTaxDueAtHigherRate) mustBe Some(BigDecimal(i))
//      }
    }
}

