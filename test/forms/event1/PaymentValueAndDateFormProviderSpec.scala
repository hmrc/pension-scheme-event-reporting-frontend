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

package forms.event1

import base.SpecBase
import forms.behaviours.{BigDecimalFieldBehaviours, DateBehavioursTrait}
import org.scalacheck.Gen
import play.api.data.FormError

import java.time.LocalDate

class PaymentValueAndDateFormProviderSpec extends SpecBase
  with BigDecimalFieldBehaviours with DateBehavioursTrait {

  private val stubMin: LocalDate = LocalDate.of(2022, 4, 6)
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5)

  private val form = new PaymentValueAndDateFormProvider().apply(stubMin.getYear)

  private val paymentValueKey = "paymentValue"
  private val paymentDateKey = "paymentDate"
  private val messageKeyPaymentValueKey = "paymentValueAndDate.value"

  // scalastyle:off magic.number
  val invalidDataGenerator: Gen[String] = decsInRangeWithCommas(0, 999999999)
  val negativeValueDataGenerator: Gen[String] = decimalsBelowValue(0)

  private def paymentDetails(
                              paymentValue: String,
                              paymentDate: Option[LocalDate]
                            ): Map[String, String] = paymentDate match {
    case Some(date) => Map(
      paymentValueKey -> paymentValue,
      paymentDateKey + ".day" -> s"${date.getDayOfMonth}",
      paymentDateKey + ".month" -> s"${date.getMonthValue}",
      paymentDateKey + ".year" -> s"${date.getYear}"
    )
    case None => Map(
      paymentValueKey -> paymentValue
    )
  }

  "paymentValue" - {

    "not bind no input" in {
      val result = form.bind(paymentDetails(paymentValue = "", Some(LocalDate.of(2022,6,4))))
      result.errors mustEqual Seq(FormError(paymentValueKey, s"$messageKeyPaymentValueKey.error.nothingEntered"))
    }

    "not bind non-numeric numbers" in {
      val result = form.bind(paymentDetails(paymentValue = "one,two.three", Some(LocalDate.of(2022,6,4))))
      result.errors mustEqual Seq(FormError(paymentValueKey, s"$messageKeyPaymentValueKey.error.notANumber"))
    }

    "not bind numbers with too many decimals" in {
      forAll(invalidDataGenerator -> "tooManyDecimals") {
        num: String =>
          val result = form.bind(paymentDetails(paymentValue = num, Some(LocalDate.of(2022,6,4))))
          result.errors mustEqual Seq(FormError(paymentValueKey, s"$messageKeyPaymentValueKey.error.tooManyDecimals"))
      }
    }

    "bind within the range 0 to 999999999.99" in {
      val number = "999999999.99"
          val result = form.bind(paymentDetails(paymentValue = number, Some(LocalDate.of(2022,6,4))))
          result.errors mustEqual Nil
    }

    "not bind outside the range 0 to 999999999.99" in {
      val number = "1000000000.00"
          val result = form.bind(paymentDetails(paymentValue = number, Some(LocalDate.of(2022,6,4))))
          result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyPaymentValueKey.error.amountTooHigh")
    }

    "not bind numbers below 0" in {
      forAll(negativeValueDataGenerator -> "negative") {
        number: String =>
          val result = form.bind(paymentDetails(paymentValue = number, Some(LocalDate.of(2022,6,4))))
          result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyPaymentValueKey.error.negative")
      }
    }
  }

  "paymentDate" - {

    behave like mandatoryDateField(
      form = form,
      key = paymentDateKey,
      requiredAllKey = "genericDate.error.invalid.allFieldsMissing"
    )

    behave like dateFieldYearNot4Digits(
      form = form,
      key = paymentDateKey,
      formError = FormError(paymentDateKey, "genericDate.error.invalid.year")
    )

    behave like dateFieldWithMin(
      form = form,
      key = paymentDateKey,
      min = stubMin,
      formError = FormError(paymentDateKey, messages("genericDate.error.outsideReportedYear", "06 April 2022", "05 April 2023"))
    )

    behave like dateFieldWithMax(
      form = form,
      key = paymentDateKey,
      max = stubMax,
      formError = FormError(paymentDateKey, messages("genericDate.error.outsideReportedYear", "06 April 2022", "05 April 2023"))
    )
  }
}
