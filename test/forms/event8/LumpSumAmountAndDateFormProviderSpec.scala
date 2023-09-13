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

package forms.event8

import base.SpecBase
import forms.behaviours.{BigDecimalFieldBehaviours, DateBehavioursTrait}
import org.scalacheck.Gen
import play.api.data.FormError
import utils.DateHelper.formatDateDMY

import java.time.LocalDate

class LumpSumAmountAndDateFormProviderSpec extends SpecBase
  with BigDecimalFieldBehaviours with DateBehavioursTrait {

  private val stubMin: LocalDate = LocalDate.of(2006, 4, 6)
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5)

  private val form = new LumpSumAmountAndDateFormProvider().apply(min = stubMin, max = stubMax)
  private val validDate = LocalDate.of(2023, 1, 1)

  private val lumpSumAmountKey = "lumpSumAmount"
  private val lumpSumDateKey = "lumpSumDate"
  private val messageKeyLumpSumValueKey = "lumpSumAmountAndDate.value"

  // scalastyle:off magic.number
  val invalidDataGenerator: Gen[String] = decsInRangeWithCommas(0, 999999999)
  val negativeValueDataGenerator: Gen[String] = decimalsBelowValue(0)

  private def lumpSumDetails(
                              lumpSumAmount: String,
                              lumpSumDate: Option[LocalDate]
                            ): Map[String, String] = lumpSumDate match {
    case Some(date) => Map(
      lumpSumAmountKey -> lumpSumAmount,
      lumpSumDateKey + ".day" -> s"${date.getDayOfMonth}",
      lumpSumDateKey + ".month" -> s"${date.getMonthValue}",
      lumpSumDateKey + ".year" -> s"${date.getYear}"
    )
    case None => Map(
      lumpSumAmountKey -> lumpSumAmount
    )
  }

  "lumpSumValue" - {

    "not bind no input" in {
      val result = form.bind(lumpSumDetails(lumpSumAmount = "", Some(validDate)))
      result.errors mustEqual Seq(FormError(lumpSumAmountKey, s"$messageKeyLumpSumValueKey.error.nothingEntered"))
    }

    "not bind non-numeric numbers" in {
      val result = form.bind(lumpSumDetails(lumpSumAmount = "one,two.three", Some(validDate)))
      result.errors mustEqual Seq(FormError(lumpSumAmountKey, s"$messageKeyLumpSumValueKey.error.notANumber"))
    }

    "not bind numbers with too many decimals" in {
      forAll(invalidDataGenerator -> "tooManyDecimals") {
        num: String =>
          val result = form.bind(lumpSumDetails(lumpSumAmount = num, Some(validDate)))
          result.errors mustEqual Seq(FormError(lumpSumAmountKey, s"$messageKeyLumpSumValueKey.error.tooManyDecimals"))
      }
    }

    "bind within the range 0 to 999999999.99" in {
      val number = "999999999.99"
      val result = form.bind(lumpSumDetails(lumpSumAmount = number, Some(validDate)))
      result.errors mustEqual Nil
    }

    "not bind outside the range 0 to 999999999.99" in {
      val number = "1000000000.00"
      val result = form.bind(lumpSumDetails(lumpSumAmount = number, Some(validDate)))
      result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyLumpSumValueKey.error.amountTooHigh")
    }

    "not bind numbers equal to 0" in {
      val number = "0.00"
      val result = form.bind(lumpSumDetails(lumpSumAmount = number, Some(validDate)))
      result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyLumpSumValueKey.error.zeroEntered")
    }

    "not bind numbers below 0" in {
      forAll(negativeValueDataGenerator -> "negative") {
        number: String =>
          val result = form.bind(lumpSumDetails(lumpSumAmount = number, Some(validDate)))
          result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyLumpSumValueKey.error.negativeValue")
      }
    }
  }

  "lumpSumDate" - {

    behave like mandatoryDateField(
      form = form,
      key = lumpSumDateKey,
      requiredAllKey = "genericDate.error.invalid.allFieldsMissing"
    )

    behave like dateFieldYearNot4Digits(
      form = form,
      key = lumpSumDateKey,
      formError = FormError(lumpSumDateKey, "genericDate.error.invalid.year")
    )

    behave like dateFieldWithMin(
      form = form,
      key = lumpSumDateKey,
      min = stubMin,
      formError = FormError(lumpSumDateKey, messages("lumpSumAmountAndDate.date.error.outsideReportedYear", formatDateDMY(stubMin), formatDateDMY(stubMax)))
    )

    behave like dateFieldWithMax(
      form = form,
      key = lumpSumDateKey,
      max = stubMax,
      formError = FormError(lumpSumDateKey, messages("lumpSumAmountAndDate.date.error.outsideReportedYear", formatDateDMY(stubMin), formatDateDMY(stubMax)))
    )
  }
}
