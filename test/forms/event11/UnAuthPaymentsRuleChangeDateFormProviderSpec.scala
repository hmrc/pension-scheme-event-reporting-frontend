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

package forms.event11

import base.SpecBase

import java.time.LocalDate
import forms.behaviours.{BigDecimalFieldBehaviours, DateBehavioursTrait}
import play.api.data.FormError
import utils.DateHelper.formatDateDMY

class UnAuthPaymentsRuleChangeDateFormProviderSpec extends SpecBase
  with BigDecimalFieldBehaviours with DateBehavioursTrait {

  private val stubMin: LocalDate = LocalDate.of(2022, 4, 6)
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5)

  private val form = new UnAuthPaymentsRuleChangeDateFormProvider()(stubMin, stubMax)
  private val dateKey = "value"

  ".value" - {

    behave like mandatoryDateField(
      form = form,
      key = dateKey,
      requiredAllKey = "genericDate.error.invalid.allFieldsMissing"
    )

    behave like dateFieldYearNot4Digits(
      form = form,
      key = dateKey,
      formError = FormError(dateKey, "genericDate.error.invalid.year")
    )


    behave like dateFieldWithMin(
      form = form,
      key = dateKey,
      min = stubMin,
      formError = FormError(dateKey, messages("unAuthPaymentsRuleChangeDate.error.outsideReportedYear", formatDateDMY(stubMin), formatDateDMY(stubMax)))
    )

    behave like dateFieldWithMax(
      form = form,
      key = dateKey,
      max = stubMax,
      formError = FormError(dateKey, messages("unAuthPaymentsRuleChangeDate.error.outsideReportedYear", formatDateDMY(stubMin), formatDateDMY(stubMax)))
    )
  }
}
