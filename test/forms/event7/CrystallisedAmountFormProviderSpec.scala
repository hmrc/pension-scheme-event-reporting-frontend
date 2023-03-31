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

import base.SpecBase
import forms.behaviours.BigDecimalFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class CrystallisedAmountFormProviderSpec extends SpecBase with BigDecimalFieldBehaviours {

  private val form = new CrystallisedAmountFormProvider()()
  private val valueKey = "crystallisedAmount"
  private val messageKeyValueKey = "amounts.value"

  // scalastyle:off magic.number
  val invalidDataGenerator: Gen[String] = intsInRangeWithCommas(0, 999999999)
  val negativeValueDataGenerator: Gen[String] = decimalsBelowValue(0.00)

  private def valueDetails(value: String): Map[String, String] = Map(valueKey -> value)

  ".value" - {

    "not bind no input" in {
      val result = form.bind(valueDetails(""))
      result.errors mustEqual Seq(FormError(valueKey, "crystallisedAmount.value.error.nothingEntered"))
    }

    "not bind non-numeric numbers" in {
      val result = form.bind(valueDetails("one,two.three"))
      result.errors mustEqual Seq(FormError(valueKey, s"$messageKeyValueKey.error.notANumber"))
    }

    "not bind integers" in {
      forAll(invalidDataGenerator -> "noDecimals") {
        int: String =>
          val result = form.bind(valueDetails(int))
          result.errors mustEqual Seq(FormError(valueKey, s"$messageKeyValueKey.error.noDecimals"))
      }
    }

    "bind within the range 0 to 999999999.99" in {
      val number = "999999999.99"
      val result = form.bind(valueDetails(number))
      result.errors mustEqual Nil
    }

    "not bind outside the range 0 to 999999999.99" in {
      val number = "1000000000.00"
      val result = form.bind(valueDetails(number))
      result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyValueKey.error.amountTooHigh")
    }

    "not bind 0.00" in {
      val number = "0.00"
      val result = form.bind(valueDetails(number))
      result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyValueKey.error.zeroAmount")
    }

    "not bind numbers below 0" in {
      forAll(negativeValueDataGenerator -> "negative") {
        number: String =>
          val result = form.bind(valueDetails(number))
          result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyValueKey.error.negative")
      }
    }
  }
}
