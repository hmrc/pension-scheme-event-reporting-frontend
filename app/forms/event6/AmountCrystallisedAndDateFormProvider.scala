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

package forms.event6

import forms.mappings.{Mappings, Transforms}
import models.event6.CrystallisedDetails
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateConstraintHandlers.{localDateMappingWithDateRange, localDatesConstraintHandler}

import java.time.LocalDate
import javax.inject.Inject

class AmountCrystallisedAndDateFormProvider @Inject() extends Mappings with Transforms { // scalastyle:off magic.number

  import AmountCrystallisedAndDateFormProvider._

  def apply(startDate:LocalDate, endDate: LocalDate)(implicit messages: Messages): Form[CrystallisedDetails] =
    Form(
      mapping("amountCrystallised" ->
        bigDecimal2DP("amountCrystallisedAndDate.value.error.nothingEntered",
          "amountCrystallisedAndDate.value.error.notANumber",
          "amountCrystallisedAndDate.value.error.tooManyDecimals")
          .verifying(
            maximumValue[BigDecimal](maxCrystallisedValue, "amountCrystallisedAndDate.value.error.amountTooHigh"),
            minimumValue[BigDecimal](0, "amountCrystallisedAndDate.value.error.negativeValue"),
            zeroValue[BigDecimal](0, "amountCrystallisedAndDate.value.error.zeroEntered")
          ),
        localDateMappingWithDateRange(field = "crystallisedDate", date = (startDate, endDate))
      )
      (CrystallisedDetails.apply)(c => Some(Tuple.fromProductTyped(c)))
    )
}

object AmountCrystallisedAndDateFormProvider {

  private val maxCrystallisedValue: BigDecimal = 999999999.99
}
