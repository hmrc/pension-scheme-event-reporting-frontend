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

package forms.event2

import forms.mappings.{Mappings, Transforms}
import play.api.data.Form
import play.api.i18n.Messages
import utils.DateConstraintHandlers.{localDateMappingWithDateRange, localDatesConstraintHandler}

import java.time.{LocalDate, Month}
import javax.inject.Inject

class DatePaidFormProvider @Inject() extends Mappings with Transforms {
  def apply(taxYear: Int)(implicit messages: Messages): Form[LocalDate] = {
    // scalastyle:off magic.number
    val startDate: LocalDate = LocalDate.of(2006, Month.APRIL, 6)
    val endDate: LocalDate = LocalDate.of(taxYear + 1, Month.APRIL, 5)
    Form(
      localDateMappingWithDateRange(date = (startDate, endDate), outOfRangeKey = "datePaid.event2.error.outside.taxYear")
    )
  }
}
