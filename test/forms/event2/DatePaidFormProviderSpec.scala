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

package forms.event2

import base.SpecBase
import forms.behaviours.{BigDecimalFieldBehaviours, DateBehavioursTrait}
import play.api.data.FormError
import utils.DateHelper.formatDateDMY

import java.time.LocalDate

class DatePaidFormProviderSpec extends SpecBase with BigDecimalFieldBehaviours with DateBehavioursTrait  {
  // scalastyle:off magic.number
  private val form = new DatePaidFormProvider()(2022)
  private val stubMin: LocalDate = LocalDate.of(2006, 4, 6)
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5)
  
  private val datePaidKey = "value"
  "datePaid" - {

    behave like mandatoryDateField(
      form = form,
      key = datePaidKey,
      requiredAllKey = "genericDate.error.invalid.allFieldsMissing"
    )

    behave like dateFieldYearNot4Digits(
      form = form,
      key = datePaidKey,
      formError = FormError(datePaidKey, "genericDate.error.invalid.year")
    )

    behave like dateFieldWithMin(
      form = form,
      key = datePaidKey,
      min = stubMin,
      formError = FormError(datePaidKey, messages("datePaid.event2.error.outside.taxYear", formatDateDMY(stubMin), formatDateDMY(stubMax)))
    )

    behave like dateFieldWithMax(
      form = form,
      key = datePaidKey,
      max = stubMax,
      formError = FormError(datePaidKey, messages("datePaid.event2.error.outside.taxYear", formatDateDMY(stubMin), formatDateDMY(stubMax)))
    )
  }
}
