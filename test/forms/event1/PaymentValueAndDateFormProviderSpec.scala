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

package forms.event1

import base.SpecBase
import forms.behaviours.{BigDecimalFieldBehaviours, DateBehavioursTrait}
import org.scalacheck.Gen
import play.api.data.FormError

import java.time.LocalDate

class PaymentValueAndDateFormProviderSpec extends SpecBase
  with BigDecimalFieldBehaviours with DateBehavioursTrait {

  // TODO: change implementation to real date once preceding pages are implemented, using stubDate for now.
  private val stubMin: LocalDate = LocalDate of(2022, 4, 6)
  private val stubMax: LocalDate = LocalDate of(2023, 4, 5)

  private val form = new PaymentValueAndDateFormProvider().apply(min = stubMin, max = stubMax)

  private val paymentValueKey = "paymentValue"
  private val paymentDateKey = "paymentDate"
  private val messageKeyPaymentValueKey = "paymentValueAndDate.value"
  // private val messageKeyPaymentDateKey = "paymentValueAndDate.date" // not yet used

  val validDataGenerator: Gen[String] = decsInRangeWithCommas(0, 999999999.99)

  private def paymentDetails(
                              paymentValue: String = "1000.00",
                              paymentDate: Option[LocalDate] = None
                            ): Map[String, String] = paymentDate match {
    case Some(date) => Map(
      paymentValueKey -> paymentValue,
      paymentDateKey + ".day" -> s"${date.getDayOfMonth}",
      paymentDateKey + ".month" -> s"${date.getMonth}",
      paymentDateKey + ".year" -> s"${date.getYear}"
    )
    case None => Map(
      paymentValueKey -> paymentValue
    )
  }

  "paymentValue" - {

    "not bind non-numeric numbers" in {
      forAll(nonNumerics -> "notANumber") {
        nonNumeric: String =>
          val result = form.bind(paymentDetails(paymentValue = nonNumeric, Some(LocalDate.now())))
          result.errors mustEqual Seq(FormError(paymentValueKey, s"$messageKeyPaymentValueKey.error.notANumber"))
      }
    }

    "not bind outside the range 0 to 999999999.99" in {
      forAll(validDataGenerator -> "amountTooHigh") {
        number: String =>
          val result = form.bind(paymentDetails(paymentValue = number, Some(LocalDate.now())))
          result.errors.head.key mustEqual messageKeyPaymentValueKey
      }
    }
  }

  //    behave like fieldThatBindsValidData(
  //      form,
  //      fieldName,
  //      validDataGenerator
  //    )
  //
  //    behave like bigDecimalField(
  //      form,
  //      fieldName,
  //      nonNumericError  = FormError(fieldName, "paymentValueAndDate.value.error.notANumber"),
  //      decimalsError = FormError(fieldName, "paymentValueAndDate.value.error.noDecimals")
  //    )
  //
  //    behave like bigDecimalFieldWithRange(
  //      form,
  //      fieldName,
  //      minimum       = minimum,
  //      maximum       = maximum,
  //      expectedError = FormError(fieldName, "paymentValueAndDate.value.error.amountTooHigh", Seq(minimum, maximum))
  //    )
  //
  //    behave like mandatoryField(
  //      form,
  //      fieldName,
  //      requiredError = FormError(fieldName, "paymentValueAndDate.value.error.nothingEntered")
  //    )

  "paymentDate" - {

    behave like dateFieldWithMin(
      form = form,
      key = paymentDateKey,
      min = stubMin,
      formError = FormError(paymentDateKey, "paymentValueAndDate.date.error.outsideRelevantTaxYear")
    )

    behave like dateFieldWithMax(
      form = form,
      key = paymentDateKey,
      max = stubMax,
      formError = FormError(paymentDateKey, "paymentValueAndDate.date.error.outsideRelevantTaxYear")
    )

    behave like mandatoryDateField(
      form = form,
      key = paymentDateKey,
      requiredAllKey = "paymentValueAndDate.date.error.nothingEntered")
  }
}
