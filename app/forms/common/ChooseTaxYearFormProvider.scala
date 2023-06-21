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

package forms.common

import forms.mappings.Mappings
import models.common.ChooseTaxYear
import models.enumeration.EventType
import models.enumeration.EventType.Event23
import play.api.data.Form

import javax.inject.Inject

class ChooseTaxYearFormProvider @Inject() extends Mappings {

  def apply(eventType: EventType): Form[ChooseTaxYear] = {

    Form(
      "value" -> enumerable[ChooseTaxYear](requiredKey = s"chooseTaxYear.event${eventType.toString}.error.required",
        invalidKey = "chooseTaxYear.event22.error.outsideRange",
        Seq(ChooseTaxYear.minimumYear.toString, ChooseTaxYear.maximumYear.toString)
      )
    )
  }
}
