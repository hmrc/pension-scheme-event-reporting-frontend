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

package models

import pages.TaxYearPage
import play.api.i18n.Messages
import play.api.libs.json.{JsString, Writes}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.DateHelper

import java.time.LocalDate

case class TaxYear(startYear: String) {
  def endYear: String = (startYear.toInt + 1).toString
}

object TaxYear extends Enumerable.Implicits {
  implicit val writes: Writes[TaxYear] = (yr: TaxYear) => JsString(yr.startYear)
  private val numberOfYearsToShow = 7

  val values: Seq[TaxYear] = {
    println(yearRange(DateHelper.today).reverse)
    yearRange(DateHelper.today).reverse
  }


  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages("chooseTaxYear.yearRangeRadio", value.startYear, value.endYear)),
        value = Some(value.startYear),
        id = Some(s"value_$index")
      )
  }

  def yearRange(currentDate: LocalDate): Seq[TaxYear] = {
    val endOfTaxYear = LocalDate.of(currentDate.getYear, 4, 5)
    val startOfTaxYear = LocalDate.of(currentDate.getYear, 4, 6)

    val currentTaxYearCalculated = if (currentDate.isBefore(startOfTaxYear)) {
      endOfTaxYear.getYear - 1
    }
    else {
      startOfTaxYear.getYear
    }
    (currentTaxYearCalculated - (numberOfYearsToShow - 1) to currentTaxYearCalculated).map(year => TaxYear(year.toString))
  }

  def getSelectedTaxYearAsString(userAnswers: UserAnswers): String = {
    userAnswers.get(TaxYearPage) match {
      case Some(taxYear) => s"${Integer.parseInt(taxYear.endYear.stripPrefix("TaxYear(").stripSuffix(")").trim)}"
      case _ => throw new RuntimeException("Tax year unavailable")
    }
  }

  implicit val enumerable: Enumerable[TaxYear] =
    Enumerable(values.map(v => v.startYear -> v): _*)
}
