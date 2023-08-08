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
import forms.behaviours.{BigDecimalFieldBehaviours, DateBehavioursTrait}
import org.scalacheck.Gen
import play.api.data.FormError
import utils.DateHelper.formatDateDMY

import java.time.LocalDate

class PaymentDateFormProviderSpec extends SpecBase
  with BigDecimalFieldBehaviours with DateBehavioursTrait {

  private val stubMin: LocalDate = LocalDate.of(2006, 4, 6)
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5)

  private val form = new PaymentDateFormProvider().apply(max = stubMax)

  private val paymentDateKey = "paymentDate"

  // scalastyle:off magic.number
  val invalidDataGenerator: Gen[String] = intsInRangeWithCommas(0, 999999999)
  val negativeValueDataGenerator: Gen[String] = decimalsBelowValue(0)

  "paymentDate" - {

    behave like mandatoryDateField(
      form = form,
      key = paymentDateKey,
      requiredAllKey = "paymentDate.date.error.nothingEntered"
    )

    behave like dateFieldYearNot4Digits(
      form = form,
      key = paymentDateKey,
      formError = FormError(paymentDateKey, "paymentDate.date.error.outsideDateRanges")
    )

    behave like dateFieldWithMin(
      form = form,
      key = paymentDateKey,
      min = stubMin,
      formError = FormError(paymentDateKey, messages("paymentDate.date.error.outsideReportedYear", formatDateDMY(stubMax)))
    )

    behave like dateFieldWithMax(
      form = form,
      key = paymentDateKey,
      max = stubMax,
      formError = FormError(paymentDateKey, messages("paymentDate.date.error.outsideReportedYear", formatDateDMY(stubMax)))
    )
  }
}
