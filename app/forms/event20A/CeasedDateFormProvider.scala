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

package forms.event20A

import forms.mappings.Mappings
import models.event20A.Event20ADate
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateHelper.formatDateDMY

import java.time.LocalDate
import javax.inject.Inject

class CeasedDateFormProvider @Inject() extends Mappings {

  def apply(min: LocalDate, max: LocalDate)(implicit messages: Messages): Form[Event20ADate] =
    Form(
      mapping("ceasedDateMasterTrust" ->
        localDate(
          oneDateComponentMissingKey = "schemeChangeDate.event20A.error.noDayMonthOrYear",
          twoDateComponentsMissingKey = "schemeChangeDate.event20A.error.noDayMonthOrYear",
          invalidKey = "schemeChangeDate.event20A.error.outsideDateRanges",
          threeDateComponentsMissingKey = "schemeChangeDate.event20A.error.nothingEntered"
        ).verifying(
          yearHas4Digits("schemeChangeDate.event20A.error.outsideDateRanges"),
          minDate(min, messages("schemeChangeDate.event20A.error.outsideReportedYear", formatDateDMY(min), formatDateDMY(max))),
          maxDate(max, messages("schemeChangeDate.event20A.error.outsideReportedYear", formatDateDMY(min), formatDateDMY(max)))
        )
      )
      (Event20ADate.apply)(Event20ADate.unapply)
    )
}
