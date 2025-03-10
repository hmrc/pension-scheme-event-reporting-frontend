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

package helpers

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, Text}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateHelper {
  def now: LocalDate = LocalDate.now()
}


object DateHelper {

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  def dateHintFromTaxYear(taxYear: Int)(implicit messages: Messages): Hint = {
    val startDate: LocalDate = LocalDate.of(taxYear, 4, 6)
    val endDate: LocalDate = LocalDate.of(taxYear + 1, 4, 5)
    dateMustBeBetweenHint(startDate, endDate)
  }

  def dateMustBeBetweenHint(startDate:LocalDate, endDate: LocalDate)(implicit messages: Messages): Hint =
    Hint(content = Text(
      messages("genericDate.hint", dateFormatter.format(startDate), dateFormatter.format(endDate), startDate.getYear.toString)
    ))

  def extractTaxYear(date: LocalDate): Int = {
    val year = date.getYear

    val taxYearDate = LocalDate.of(year, 4, 6)

    if (date.isBefore(taxYearDate)) {
      year - 1
    } else {
      year
    }
  }
}

