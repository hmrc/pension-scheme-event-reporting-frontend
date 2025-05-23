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

package forms.event24

import forms.mappings.{Mappings, Transforms}
import models.event24.CrystallisedDate
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateConstraintHandlers.{localDateMappingWithDateRange, localDatesConstraintHandler}

import java.time.LocalDate
import javax.inject.Inject

class CrystallisedDateFormProvider @Inject() extends Mappings with Transforms { // scalastyle:off magic.number

  def apply(min: LocalDate, max: LocalDate)(implicit messages: Messages): Form[CrystallisedDate] =
    Form(
      mapping(
        localDateMappingWithDateRange(
          field = "crystallisedDate", date = (min, max))
      )
      (CrystallisedDate.apply)(c => Some(c.date))
    )
}
