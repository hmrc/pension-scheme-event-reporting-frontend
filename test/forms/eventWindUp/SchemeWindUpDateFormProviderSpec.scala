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

import base.SpecBase
import forms.behaviours.DateBehaviours
import play.api.data.FormError
import utils.DateHelper.formatDateDMY

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SchemeWindUpDateFormProviderSpec extends DateBehaviours with SpecBase {

  private val openDate = LocalDate.of(2022, 5, 1)
  private val form = new SchemeWindUpDateFormProvider()(2022, openDate)
  private val validData = datesBetween(
    min = LocalDate.of(2022, 4, 6),
    max = LocalDate.of(2023, 4, 5)
  )

  ".value must" - {

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "schemeWindUpDate.error.required.all")

    behave like dateFieldWithMax(
      form = form,
      key = "value" ,
      max = LocalDate.of(2023,4,5),
      formError = FormError("value", "schemeWindUpDate.error.outside.taxYear", Seq("2022", "2023"))
    )

    behave like dateFieldWithMin(
      form = form,
      key = "value" ,
      min = LocalDate.of(2022,4,6),
      formError = FormError("value", "schemeWindUpDate.error.outside.taxYear", Seq("2022", "2023"))
    )

    s"must fail to bind a date earlier than openDate ${openDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}" in {
      val date = LocalDate.of(2022, 5, 1)
      val data = Map(
        s"$key.day" -> date.getDayOfMonth.toString,
        s"$key.month" -> date.getMonthValue.toString,
        s"$key.year" -> date.getYear.toString
      )

      val result = form.bind(data)

      result.errors must contain(FormError("value", "schemeWindUpDate.error.beforeOpenDate", Seq(openDate)))
    }

      }
}
