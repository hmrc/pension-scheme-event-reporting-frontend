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

package forms.eventWindUp

import forms.mappings.Mappings
import models.TaxYearValidationDetail
import play.api.data.Form
import utils.DateHelper.formatDateDMY

import java.time.LocalDate
import javax.inject.Inject

class SchemeWindUpDateFormProvider @Inject() extends Mappings {

  def apply(taxYear: Int, openDate: LocalDate): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey = "schemeWindUpDate.error.invalid",
        threeDateComponentsMissingKey = "schemeWindUpDate.error.required.all",
        twoDateComponentsMissingKey = "schemeWindUpDate.error.required.two",
        oneDateComponentMissingKey = "schemeWindUpDate.error.required",
        taxYearValidationDetail = Some(TaxYearValidationDetail(
          invalidKey = "schemeWindUpDate.error.outside.taxYear",
          taxYear = taxYear
        ))
      ).verifying(
        isNotBeforeOpenDate(openDate, "schemeWindUpDate.error.beforeOpenDate", formatDateDMY(openDate))
      )


    )
}
