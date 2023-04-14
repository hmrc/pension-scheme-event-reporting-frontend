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

package helpers

import models.UserAnswers
import pages.TaxYearPage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}

class DateHelper {
  def now = LocalDate.now()
}


object DateHelper {

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  def extractTaxYear(date: LocalDate): Int = {
    val year = date.getYear

    val taxYearDate = LocalDate.of(year, 4, 6)

    if (date.isBefore(taxYearDate)) {
      year - 1
    } else {
      year
    }
  }

  def getTaxYear(userAnswers: Option[UserAnswers]): Int = {
    userAnswers.flatMap(_.get(TaxYearPage)) match {
      case Some(year) => Try(year.startYear.toInt) match {
        case Success(value) => value
        case Failure(exception) => throw new RuntimeException("Tax year is not a number", exception)
      }
      case _ => throw new RuntimeException("Tax year not entered")
    }
  }
}

