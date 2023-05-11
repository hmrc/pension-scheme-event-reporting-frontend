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

package forms.event12

import forms.mappings.{Mappings, Transforms}
import models.event12.DateOfChange
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateHelper.formatDateDMY

import java.time.LocalDate
import javax.inject.Inject

class DateOfChangeFormProvider @Inject() extends Mappings with Transforms {

  def apply(min: LocalDate, max: LocalDate)(implicit messages: Messages): Form[DateOfChange] =
    Form(
      mapping("dateOfChange" ->
        localDate(
          oneDateComponentMissingKey = "dateOfChange.error.noDayMonthOrYear",
          twoDateComponentsMissingKey = "dateOfChange.error.noDayMonthOrYear",
          invalidKey = "dateOfChange.error.outsideDateRanges",
          threeDateComponentsMissingKey = "dateOfChange.error.nothingEntered"
        ).verifying(
          yearHas4Digits("dateOfChange.error.outsideDateRanges"),
          minDate(min, messages("dateOfChange.error.outsideReportedYear", formatDateDMY(min), formatDateDMY(max))),
          maxDate(max, messages("dateOfChange.error.outsideReportedYear", formatDateDMY(min), formatDateDMY(max)))
        )
      )
      (DateOfChange.apply)(DateOfChange.unapply)
    )
}
