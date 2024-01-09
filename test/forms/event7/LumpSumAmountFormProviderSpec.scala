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

package forms.event7

import base.SpecBase
import forms.behaviours.BigDecimalFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class LumpSumAmountFormProviderSpec extends SpecBase with BigDecimalFieldBehaviours {

  private val form = new LumpSumAmountFormProvider()()
  private val valueKey = "lumpSumAmount"
  private val messageKeyValueKey = "amounts.value"

  // scalastyle:off magic.number
  val invalidDataGenerator: Gen[String] = decsInRangeWithCommas(0, 999999999)
  val negativeValueDataGenerator: Gen[String] = decimalsBelowValue(0.00)

  private def valueDetails(value: String): Map[String, String] = Map(valueKey -> value)

  ".value" - {

    "not bind no input" in {
      val result = form.bind(valueDetails(""))
      result.errors mustEqual Seq(FormError(valueKey, "lumpSumAmount.value.error.nothingEntered"))
    }

    "not bind non-numeric numbers" in {
      val result = form.bind(valueDetails("one,two.three"))
      result.errors mustEqual Seq(FormError(valueKey, s"$messageKeyValueKey.error.notANumber"))
    }

    "not bind numbers with too many decimals" in {
      forAll(invalidDataGenerator -> "tooManyDecimals") {
        num: String =>
          val result = form.bind(valueDetails(num))
          result.errors mustEqual Seq(FormError(valueKey, s"$messageKeyValueKey.error.tooManyDecimals"))
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
