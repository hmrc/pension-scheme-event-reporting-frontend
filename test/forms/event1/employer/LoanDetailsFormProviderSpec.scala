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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class LoanDetailsFormProviderSpec extends StringFieldBehaviours {
  private val form = new LoanDetailsFormProvider()()

  private val loanAmountKey = "loanAmount"
  private val fundValueKey = "fundValue"

  private val loanAmountRequiredKey = "loanDetails.loanAmount.nothingEntered"
  private val loanAmountNotANumberErrorKey = "loanDetails.loanAmount.notANumber"
  private val loanAmountTooManyDecimalsKey = "loanDetails.loanAmount.tooManyDecimals"
  private val loanAmountNegativeKey = "loanDetails.loanAmount.negative"
  private val loanAmountAmountTooHighErrorKey = "loanDetails.loanAmount.amountTooHigh"
  private val loanAmountMustNotBeZero = "loanDetails.loanAmount.zeroAmount"

  private val fundValueRequiredKey = "loanDetails.fundValue.nothingEntered"
  private val fundValueNotANumberErrorKey = "loanDetails.fundValue.notANumber"
  private val fundValueTooManyDecimalsKey = "loanDetails.fundValue.tooManyDecimals"
  private val fundValueNegativeKey = "loanDetails.fundValue.negative"
  private val fundValueAmountTooHighErrorKey = "loanDetails.fundValue.amountTooHigh"
  private val fundValueMustNotBeZero = "loanDetails.fundValue.zeroAmount"


  private def details(loanAmount: String = "12.34",
                      fundValue: String = "1.00"
                     ): Map[String, String] =
    Map(
      loanAmountKey -> loanAmount,
      fundValueKey -> fundValue
    )

  "loanAmount" - {
    behave like mandatoryField(
      form,
      loanAmountKey,
      requiredError = FormError(loanAmountKey, loanAmountRequiredKey)
    )

    "not bind non-numeric numbers" in {
      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(details(loanAmount = nonNumeric))
          result.errors mustEqual Seq(FormError(loanAmountKey, loanAmountNotANumberErrorKey))
      }
    }

    "not bind decimals that are greater than 2 dp" in {
      forAll(decimals -> "decimal") {
        decimal =>
          val result = form.bind(details(loanAmount = decimal))
          result.errors mustEqual Seq(FormError(loanAmountKey, loanAmountTooManyDecimalsKey))
      }
    }

    "not bind negative values" in {
      forAll(decimalsBelowValue(0) -> "negative") {
        decimal =>
          val result = form.bind(details(loanAmount = decimal))
          result.errors.headOption.map(_.message) mustEqual Some(loanAmountNegativeKey)
      }
    }

    "not bind decimals longer than 11 characters" in {
      forAll(longDecimalString(12) -> "decimalAboveMax") {
        decimal =>
          val result = form.bind(details(loanAmount = decimal))
          result.errors.headOption.map(_.message) mustEqual Some(loanAmountAmountTooHighErrorKey)
      }
    }

    "not bind 0.00" in {
      val result = form.bind(details(loanAmount = "0.00"))
      result.errors.headOption.map(_.message) mustEqual Some(loanAmountMustNotBeZero)
    }
  }

  "fundValue" - {
    behave like mandatoryField(
      form,
      fundValueKey,
      requiredError = FormError(fundValueKey, fundValueRequiredKey)
    )

    "not bind non-numeric numbers" in {
      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(details(fundValue = nonNumeric))
          result.errors mustEqual Seq(FormError(fundValueKey, fundValueNotANumberErrorKey))
      }
    }

    "not bind decimals that are greater than 2 dp" in {
      forAll(decimals -> "decimal") {
        decimal =>
          val result = form.bind(details(fundValue = decimal))
          result.errors mustEqual Seq(FormError(fundValueKey, fundValueTooManyDecimalsKey))
      }
    }

    "not bind negative values" in {
      forAll(decimalsBelowValue(0) -> "negative") {
        decimal =>
          val result = form.bind(details(fundValue = decimal))
          result.errors.headOption.map(_.message) mustEqual Some(fundValueNegativeKey)
      }
    }

    "not bind decimals longer than 11 characters" in {
      forAll(longDecimalString(12) -> "decimalAboveMax") {
        decimal =>
          val result = form.bind(details(fundValue = decimal))
          result.errors.headOption.map(_.message) mustEqual Some(fundValueAmountTooHighErrorKey)
      }
    }

    "not bind 0.00" in {
      val result = form.bind(details(fundValue = "0.00"))
      result.errors.headOption.map(_.message) mustEqual Some(fundValueMustNotBeZero)
    }
  }
}

