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

package forms.event11

import java.time.LocalDate
import forms.mappings.Mappings
import models.event11.Event11Date

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateHelper.formatDateDMY

class UnAuthPaymentsRuleChangeDateFormProvider @Inject() extends Mappings {

  def apply(min: LocalDate, max: LocalDate)(implicit messages: Messages): Form[Event11Date] =
    Form(
      mapping("value" -> localDate(
        invalidKey = "unAuthPaymentsRuleChangeDate.error.invalid",
        threeDateComponentsMissingKey = "unAuthPaymentsRuleChangeDate.error.required.all",
        twoDateComponentsMissingKey = "unAuthPaymentsRuleChangeDate.error.required.two",
        oneDateComponentMissingKey = "unAuthPaymentsRuleChangeDate.error.required"
      ).verifying(
        yearHas4Digits("unAuthPaymentsRuleChangeDate.error.outsideDateRanges"),
        minDate(min, messages("unAuthPaymentsRuleChangeDate.error.outsideReportedYear", formatDateDMY(min), formatDateDMY(max))),
        maxDate(max, messages("unAuthPaymentsRuleChangeDate.error.outsideReportedYear", formatDateDMY(min), formatDateDMY(max)))
      )
      )
      (Event11Date.apply)(Event11Date.unapply)
    )
}