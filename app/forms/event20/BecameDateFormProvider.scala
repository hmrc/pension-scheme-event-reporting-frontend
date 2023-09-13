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

package forms.event20

import models.event20.Event20Date
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateConstraintHandlers.{localDateMappingWithDateRange, localDatesConstraintHandler}

import java.time.LocalDate

class BecameDateFormProvider {

  def apply(min: LocalDate, max: LocalDate)(implicit messages: Messages): Form[Event20Date] =
    Form(
      mapping(
        localDateMappingWithDateRange(field = "becameDate", date = (min, max), outOfRangeKey = "schemeChangeDate.error.outsideReportedYear")
      )
      (Event20Date.apply)(Event20Date.unapply)
    )
}
