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

package forms.event6

import base.SpecBase
import forms.behaviours.{BigDecimalFieldBehaviours, DateBehavioursTrait}
import org.scalacheck.Gen
import play.api.data.FormError
import utils.DateHelper.formatDateDMY

import java.time.LocalDate

class AmountCrystallisedAndDateFormProviderSpec extends SpecBase
  with BigDecimalFieldBehaviours with DateBehavioursTrait {

  private val stubMin: LocalDate = LocalDate.of(2006, 4, 6)
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5)

  private val validDate = LocalDate.of(2023, 1, 1)

  private val form = new AmountCrystallisedAndDateFormProvider().apply(max = stubMax)

  private val amountCrystallisedKey = "amountCrystallised"
  private val crystallisedDateKey = "crystallisedDate"
  private val messageKeyPaymentValueKey = "amountCrystallisedAndDate.value"

  // scalastyle:off magic.number
  val invalidDataGenerator: Gen[String] = decsInRangeWithCommas(0, 999999999)
  val negativeValueDataGenerator: Gen[String] = decimalsBelowValue(0)

  private def crystallisedDetails(
                                   amountCrystallised: String,
                                   crystallisedDate: Option[LocalDate]
                                 ): Map[String, String] = crystallisedDate match {
    case Some(date) => Map(
      amountCrystallisedKey -> amountCrystallised,
      crystallisedDateKey + ".day" -> s"${date.getDayOfMonth}",
      crystallisedDateKey + ".month" -> s"${date.getMonthValue}",
      crystallisedDateKey + ".year" -> s"${date.getYear}"
    )
    case None => Map(
      amountCrystallisedKey -> amountCrystallised
    )
  }

  "paymentValue" - {

    "not bind no input" in {
      val result = form.bind(crystallisedDetails(amountCrystallised = "", Some(validDate)))
      result.errors mustEqual Seq(FormError(amountCrystallisedKey, s"$messageKeyPaymentValueKey.error.nothingEntered"))
    }

    "not bind non-numeric numbers" in {
      val result = form.bind(crystallisedDetails(amountCrystallised = "one,two.three", Some(validDate)))
      result.errors mustEqual Seq(FormError(amountCrystallisedKey, s"$messageKeyPaymentValueKey.error.notANumber"))
    }

    "not bind numbers with too many decimals" in {
      forAll(invalidDataGenerator -> "tooManyDecimals") {
        num: String =>
          val result = form.bind(crystallisedDetails(amountCrystallised = num, Some(validDate)))
          result.errors mustEqual Seq(FormError(amountCrystallisedKey, s"$messageKeyPaymentValueKey.error.tooManyDecimals"))
      }
    }

    "bind within the range 0 to 999999999.99" in {
      val number = "999999999.99"
      val result = form.bind(crystallisedDetails(amountCrystallised = number, Some(validDate)))
      result.errors mustEqual Nil
    }

    "not bind outside the range 0 to 999999999.99" in {
      val number = "1000000000.00"
      val result = form.bind(crystallisedDetails(amountCrystallised = number, Some(validDate)))
      result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyPaymentValueKey.error.amountTooHigh")
    }

    "not bind numbers equal to 0" in {
      val number = "0.00"
      val result = form.bind(crystallisedDetails(amountCrystallised = number, Some(validDate)))
      result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyPaymentValueKey.error.zeroEntered")
    }

    "not bind numbers below 0" in {
      forAll(negativeValueDataGenerator -> "negative") {
        number: String =>
          val result = form.bind(crystallisedDetails(amountCrystallised = number, Some(validDate)))
          result.errors.headOption.map(_.message) mustEqual Some(s"$messageKeyPaymentValueKey.error.negativeValue")
      }
    }
  }

  "paymentDate" - {

    behave like mandatoryDateField(
      form = form,
      key = crystallisedDateKey,
      requiredAllKey = "genericDate.error.invalid.allFieldsMissing"
    )

    behave like dateFieldYearNot4Digits(
      form = form,
      key = crystallisedDateKey,
      formError = FormError(crystallisedDateKey, "genericDate.error.invalid")
    )

    behave like dateFieldWithMin(
      form = form,
      key = crystallisedDateKey,
      min = stubMin,
      formError = FormError(crystallisedDateKey, messages("amountCrystallisedAndDate.date.error.outsideReportedYear", formatDateDMY(stubMin), formatDateDMY(stubMax)))
    )

    behave like dateFieldWithMax(
      form = form,
      key = crystallisedDateKey,
      max = stubMax,
      formError = FormError(crystallisedDateKey, messages("amountCrystallisedAndDate.date.error.outsideReportedYear", formatDateDMY(stubMin), formatDateDMY(stubMax)))
    )
  }
}
