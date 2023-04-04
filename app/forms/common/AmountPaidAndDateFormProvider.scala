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

package forms.common

import forms.mappings.{Mappings, Transforms}
import models.common.PaymentDetails
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateHelper.formatDateDMY

import java.time.LocalDate
import javax.inject.Inject

class AmountPaidAndDateFormProvider @Inject() extends Mappings with Transforms {

  import AmountPaidAndDateFormProvider._

  def apply(min: LocalDate, max: LocalDate)(implicit messages: Messages): Form[PaymentDetails] =
    Form(
      mapping("amountPaid" ->
        bigDecimal2DP("amountPaidAndDate.value.error.nothingEntered",
          "amountPaidAndDate.value.error.notANumber",
          "amountPaidAndDate.value.error.noDecimals")
          .verifying(
            maximumValue[BigDecimal](maxPaymentValue, "amountPaidAndDate.value.error.amountTooHigh"),
            minimumValue[BigDecimal](0, "amountPaidAndDate.value.error.negativeValue"),
            zeroValue[BigDecimal](0, "amountPaidAndDate.value.error.zeroEntered")
          ), "eventDate" ->
        localDate(
          oneDateComponentMissingKey = "amountPaidAndDate.date.error.noDayMonthOrYear",
          twoDateComponentsMissingKey = "amountPaidAndDate.date.error.noDayMonthOrYear",
          invalidKey = "amountPaidAndDate.date.error.outsideDateRanges",
          threeDateComponentsMissingKey = "amountPaidAndDate.date.error.nothingEntered"
        ).verifying(
          yearHas4Digits("amountPaidAndDate.date.error.outsideDateRanges"),
          minDate(min, messages("amountPaidAndDate.date.error.outsideReportedYear", formatDateDMY(min), formatDateDMY(max))),
          maxDate(max, messages("amountPaidAndDate.date.error.outsideReportedYear", formatDateDMY(min), formatDateDMY(max)))
        )
      )
      (PaymentDetails.apply)(PaymentDetails.unapply)
    )
}

object AmountPaidAndDateFormProvider {
  private val maxPaymentValue: BigDecimal = 999999999.99
}
