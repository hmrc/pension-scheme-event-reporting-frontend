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
import scala.util.{Failure, Success, Try}

case class TaxYear(startYear: String) {
  def endYear: String = (startYear.toInt + 1).toString

  def rangeAsSeqString: Seq[String] = Seq(startYear, endYear)
}

object TaxYear extends Enumerable.Implicits {
  implicit val writes: Writes[TaxYear] = (yr: TaxYear) => JsString(yr.startYear)
  private val numberOfYearsToShow = 7

  val values: Seq[TaxYear] = {
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

  def optionsFiltered(func: TaxYear => Boolean)(implicit messages: Messages): Seq[RadioItem] = {
    values.zipWithIndex.flatMap {
      case (value, index) =>
        if (func(value)) {
          Seq(RadioItem(
            content = Text(messages("chooseTaxYear.yearRangeRadio", value.startYear, value.endYear)),
            value = Some(value.startYear),
            id = Some(s"value_$index")
          )
          )
        } else {
          Nil
        }
    }
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

  def getSelectedTaxYear(userAnswers: UserAnswers): TaxYear = {
    userAnswers.get(TaxYearPage) match {
      case Some(taxYear) => taxYear
      case _ => throw new RuntimeException("Tax year unavailable")
    }
  }
  def getSelectedTaxYearAsString(userAnswers: UserAnswers): String = {
    val taxYear = getSelectedTaxYear(userAnswers)
    s"${Integer.parseInt(taxYear.endYear.stripPrefix("TaxYear(").stripSuffix(")").trim)}"
  }

  def getTaxYearFromOption(userAnswers: Option[UserAnswers]): Int = {
    userAnswers.map(y => getTaxYear(y)).getOrElse(throw new RuntimeException("Tax year not entered"))
  }

  def getTaxYear(userAnswers: UserAnswers): Int = {
    userAnswers.get(TaxYearPage) match {
      case Some(year) => Try(year.startYear.toInt) match {
        case Success(value) => value
        case Failure(exception) => throw new RuntimeException("Tax year is not a number", exception)
      }
      case _ => throw new RuntimeException("Tax year not entered")
    }
  }

  implicit val enumerable: Enumerable[TaxYear] =
    Enumerable(values.map(v => v.startYear -> v): _*)
}
