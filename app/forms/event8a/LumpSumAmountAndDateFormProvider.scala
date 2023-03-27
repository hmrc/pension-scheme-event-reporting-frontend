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

package forms.event8a

import forms.mappings.{Mappings, Transforms}
import models.event8a.LumpSumDetails
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateHelper.formatDateDMY

import java.time.LocalDate
import javax.inject.Inject

class LumpSumAmountAndDateFormProvider @Inject() extends Mappings with Transforms {

  import forms.event8a.LumpSumAmountAndDateFormProvider._

  def apply(min: LocalDate, max: LocalDate)(implicit messages: Messages): Form[LumpSumDetails] =
    Form(
      mapping("lumpSumAmount" ->
        bigDecimal2DP("event8a.lumpSumAmountAndDate.value.error.nothingEntered",
          "event8a.lumpSumAmountAndDate.value.error.notANumber",
          "event8a.lumpSumAmountAndDate.value.error.noDecimals")
          .verifying(
            maximumValue[BigDecimal](maxLumpSumValue, "event8a.lumpSumAmountAndDate.value.error.amountTooHigh"),
            minimumValue[BigDecimal](0, "event8a.lumpSumAmountAndDate.value.error.negativeValue"),
            zeroValue[BigDecimal](0, "event8a.lumpSumAmountAndDate.value.error.zeroEntered")
          ), "lumpSumDate" ->
        localDate(
          oneDateComponentMissingKey = "event8a.lumpSumAmountAndDate.date.error.noDayMonthOrYear",
          twoDateComponentsMissingKey = "event8a.lumpSumAmountAndDate.date.error.noDayMonthOrYear",
          invalidKey = "event8a.lumpSumAmountAndDate.date.error.outsideDateRanges",
          threeDateComponentsMissingKey = "event8a.lumpSumAmountAndDate.date.error.nothingEntered"
        ).verifying(
          yearHas4Digits("event8a.lumpSumAmountAndDate.date.error.outsideDateRanges"),
          minDate(min, messages("event8a.lumpSumAmountAndDate.date.error.outsideReportedYear", formatDateDMY(min), formatDateDMY(max))),
          maxDate(max, messages("event8a.lumpSumAmountAndDate.date.error.outsideReportedYear", formatDateDMY(min), formatDateDMY(max)))
        )
      )
      (LumpSumDetails.apply)(LumpSumDetails.unapply)
    )
}

object LumpSumAmountAndDateFormProvider {
  private val maxLumpSumValue: BigDecimal = 999999999.99
}
