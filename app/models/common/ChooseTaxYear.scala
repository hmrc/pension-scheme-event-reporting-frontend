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

package models.common

import models.Enumerable
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.DateHelper

import java.time.{LocalDate, Month}

case class ChooseTaxYear(startYear: String) {
  override def toString: String = startYear
}

object ChooseTaxYear extends Enumerable.Implicits {

  private final val startDayOfNewTaxYear: Int = 6
  final val minimumYear: Int = 2013

  def maximumYear: Int = {
    val currentYear = DateHelper.today.getYear
    val newTaxYearStart = LocalDate.of(currentYear, Month.APRIL.getValue, startDayOfNewTaxYear)

    if (DateHelper.today.isBefore(newTaxYearStart)) {
      currentYear - 1
    } else {
      currentYear
    }
  }

  def values: Seq[ChooseTaxYear] = valueMinYear(minimumYear)

  private def valueMinYear(minYear: Int): Seq[ChooseTaxYear] = {
    val currentYear = DateHelper.today.getYear
    val newTaxYearStart = LocalDate.of(currentYear, Month.APRIL.getValue, startDayOfNewTaxYear)

    val maxYear =
      if (DateHelper.today.isBefore(newTaxYearStart)) {
        currentYear - 1
      } else {
        currentYear
      }
    (minYear to maxYear).reverse.map(year => ChooseTaxYear(year.toString))
  }

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      val yearRangeOption = messages("chooseTaxYear.yearRangeRadio", value, (value.toString.toInt + 1).toString)
      RadioItem(
        content = Text(yearRangeOption),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[ChooseTaxYear] =
    Enumerable(values.map(value => value.toString -> value): _*)
}
