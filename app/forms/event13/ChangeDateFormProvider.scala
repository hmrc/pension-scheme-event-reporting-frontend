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

package forms.event13

import java.time.LocalDate
import forms.mappings.Mappings
import models.TaxYearValidationDetail

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages

class ChangeDateFormProvider @Inject() extends Mappings {

  def apply(taxYear:Int)(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey = "genericDate.error.invalid",
        taxYearValidationDetail = Some(TaxYearValidationDetail(
          invalidKey = "event13.changeDate.error.outside.taxYear",
          taxYear = taxYear
        ))
      )
    )
}
