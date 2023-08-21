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

package forms.event10

import base.SpecBase
import forms.behaviours.{BigDecimalFieldBehaviours, DateBehavioursTrait}
import play.api.data.FormError
import utils.DateHelper.formatDateDMY

import java.time.LocalDate

class SchemeChangeDateFormProviderSpec extends SpecBase
  with BigDecimalFieldBehaviours with DateBehavioursTrait {

  private val stubMin: LocalDate = LocalDate.of(2022, 4, 6)
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5)

  private val form = new SchemeChangeDateFormProvider().apply(min = stubMin, max = stubMax)
  private val schemeChangeDateKey = "schemeChangeDate"

  "schemeChangeDate" - {

    behave like mandatoryDateField(
      form = form,
      key = schemeChangeDateKey,
      requiredAllKey = "genericDate.error.invalid.allFieldsMissing"
    )

    behave like dateFieldYearNot4Digits(
      form = form,
      key = schemeChangeDateKey,
      formError = FormError(schemeChangeDateKey, "genericDate.error.invalid.year")
    )

    behave like dateFieldWithMin(
      form = form,
      key = schemeChangeDateKey,
      min = stubMin,
      formError = FormError(schemeChangeDateKey, messages("schemeChangeDate.error.outsideReportedYear", formatDateDMY(stubMin), formatDateDMY(stubMax)))
    )

    behave like dateFieldWithMax(
      form = form,
      key = schemeChangeDateKey,
      max = stubMax,
      formError = FormError(schemeChangeDateKey, messages("schemeChangeDate.error.outsideReportedYear", formatDateDMY(stubMin), formatDateDMY(stubMax)))
    )
  }
}
