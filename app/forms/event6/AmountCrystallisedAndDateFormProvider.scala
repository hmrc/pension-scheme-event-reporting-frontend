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

import forms.mappings.{Mappings, Transforms}
import models.event6.CrystallisedDetails
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateHelper.formatDateDMY

import java.time.LocalDate
import javax.inject.Inject

class AmountCrystallisedAndDateFormProvider @Inject() extends Mappings with Transforms { // scalastyle:off magic.number

  import AmountCrystallisedAndDateFormProvider._

  private def startDate(date: LocalDate): LocalDate = {
    date match {
      case _ if date.isBefore(LocalDate.of(date.getYear, 4, 6)) =>
        LocalDate.of(date.getYear - 1, 4, 6)
      case _ =>
        LocalDate.of(date.getYear, 4, 6)
    }
  }

  private def endDate(date: LocalDate): LocalDate = {
    date match {
      case _ if date.isBefore(LocalDate.of(date.getYear, 4, 6)) =>
        LocalDate.of(date.getYear, 4, 5)
      case _ =>
        LocalDate.of(date.getYear + 1, 4, 5)
    }
  }

  def apply(min: LocalDate, max: LocalDate)(implicit messages: Messages): Form[CrystallisedDetails] =
    Form(
      mapping("amountCrystallised" ->
        bigDecimal2DP("amountCrystallisedAndDate.value.error.nothingEntered",
          "amountCrystallisedAndDate.value.error.notANumber",
          "amountCrystallisedAndDate.value.error.noDecimals")
          .verifying(
            maximumValue[BigDecimal](maxCrystallisedValue, "amountCrystallisedAndDate.value.error.amountTooHigh"),
            minimumValue[BigDecimal](0, "amountCrystallisedAndDate.value.error.negativeValue"),
            zeroValue[BigDecimal](0, "amountCrystallisedAndDate.value.error.zeroEntered")
          ), "crystallisedDate" ->
        localDate(
          oneDateComponentMissingKey = "amountCrystallisedAndDate.date.error.noDayMonthOrYear",
          twoDateComponentsMissingKey = "amountCrystallisedAndDate.date.error.noDayMonthOrYear",
          invalidKey = "amountCrystallisedAndDate.date.error.notANumber",
          threeDateComponentsMissingKey = "amountCrystallisedAndDate.date.error.nothingEntered"
        ).verifying(
          yearHas4Digits("amountCrystallisedAndDate.date.error.outsideDateRanges"),
          minDate(startDate(min), messages("amountCrystallisedAndDate.date.error.outsideReportedYear", formatDateDMY(startDate(min)), formatDateDMY(endDate(max)))),
          maxDate(endDate(max), messages("amountCrystallisedAndDate.date.error.outsideReportedYear", formatDateDMY(startDate(min)), formatDateDMY(endDate(max))))
        )
      )
      (CrystallisedDetails.apply)(CrystallisedDetails.unapply)
    )
}

object AmountCrystallisedAndDateFormProvider {
  private val maxCrystallisedValue: BigDecimal = 999999999.99
}
