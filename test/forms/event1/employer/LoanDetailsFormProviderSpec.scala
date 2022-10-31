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
  private val loanAmountNoDecimalsKey = "loanDetails.loanAmount.noDecimals"
  private val loanAmountNegativeKey = "loanDetails.loanAmount.negative"
  private val loanAmountAmountTooHighErrorKey = "loanDetails.loanAmount.amountTooHigh"

  private val fundValueNotANumberErrorKey = "loanDetails.fundValue.notANumber"
  private val fundValueNoDecimalsKey = "loanDetails.fundValue.noDecimals"
  private val fundValueNegativeKey = "loanDetails.fundValue.negative"
  private val fundValueAmountTooHighErrorKey = "loanDetails.fundValue.amountTooHigh"

  private def details(loanAmount: String = "12.34",
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
          result.errors mustEqual Seq(FormError(loanAmountKey, loanAmountNoDecimalsKey))
      }
    }

    "not bind negative values" in {
      forAll(decimalsBelowValue(0) -> "negative") {
        decimal: String =>
          val result = form.bind(details(loanAmount = decimal))
          result.errors.headOption.map(_.message) mustEqual Some(loanAmountNegativeKey)
      }
    }

    "not bind decimals longer than 11 characters" in {
      forAll(longDecimalString(12) -> "decimalAboveMax") {
        decimal: String =>
          val result = form.bind(details(loanAmount = decimal))
          result.errors.headOption.map(_.message) mustEqual Some(loanAmountAmountTooHighErrorKey)
      }
    }

    "bind 0.00 when positive value bound" in {
      val result = form.bind(details(loanAmount = "0.00"))
      result.errors mustBe Seq.empty
    }

    "not bind integers" in {
      forAll(intsInRange(0, 999999) -> "noDecimals") {
        int: String =>
          val result = form.bind(details(loanAmount = int))
          result.errors mustEqual Seq(FormError(loanAmountKey, loanAmountNoDecimalsKey))
      }
    }
  }

  "fundValue" - {
    "not bind non-numeric numbers" in {
      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric: String =>
          val result = form.bind(details(fundValue = nonNumeric))
          result.errors mustEqual Seq(FormError(fundValueKey, fundValueNotANumberErrorKey))
      }
    }

    "not bind decimals that are greater than 2 dp" in {
      forAll(decimals -> "decimal") {
        decimal: String =>
          val result = form.bind(details(fundValue = decimal))
          result.errors mustEqual Seq(FormError(fundValueKey, fundValueNoDecimalsKey))
      }
    }

    "not bind negative values" in {
      forAll(decimalsBelowValue(0) -> "negative") {
        decimal: String =>
          val result = form.bind(details(fundValue = decimal))
          result.errors.headOption.map(_.message) mustEqual Some(fundValueNegativeKey)
      }
    }

    "not bind decimals longer than 11 characters" in {
      forAll(longDecimalString(12) -> "decimalAboveMax") {
        decimal: String =>
          val result = form.bind(details(fundValue = decimal))
          result.errors.headOption.map(_.message) mustEqual Some(fundValueAmountTooHighErrorKey)
      }
    }

    "bind 0.00 when positive value" in {
      val result = form.bind(details(fundValue = "0.00"))
      result.errors mustBe Seq.empty
    }

    "not bind integers" in {
      forAll(intsInRange(0, 999999) -> "noDecimals") {
        int: String =>
          val result = form.bind(details(fundValue = int))
          result.errors mustEqual Seq(FormError(fundValueKey, fundValueNoDecimalsKey))
      }
    }
  }
}

