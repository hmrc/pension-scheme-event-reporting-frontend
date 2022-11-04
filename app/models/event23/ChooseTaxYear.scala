/*
 * Copyright 2022 HM Revenue & Customs
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

package models.event23

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

  private val startDayOfNewTaxYear: Int = 6
  private val minYear: Int = 2015
  def currentYear = new ChooseTaxYear(DateHelper.today.getYear.toString)

  def values: Seq[ChooseTaxYear] = {
    val currentYear = DateHelper.today.getYear
    val newTaxYearStart = LocalDate.of(currentYear, Month.APRIL.getValue, startDayOfNewTaxYear)

    val maxYear =
      if (DateHelper.today.isAfter(newTaxYearStart) || DateHelper.today.isEqual(newTaxYearStart)) {
        currentYear
      } else {
        currentYear - 1
      }

    (minYear to maxYear).reverseIterator.map(year => ChooseTaxYear(year.toString)).toSeq
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
