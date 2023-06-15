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
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import scala.language.implicitConversions

case class ChooseTaxYear(startYear: String) {
  override def toString: String = startYear
}

object ChooseTaxYear extends Enumerable.Implicits {

  private final val startDayOfNewTaxYear: Int = 6
  private final val minYearDefault: Int = 2013
  private final val minYearEvent22: Int = 2013
  private final val minYearEvent23: Int = 2015

  def values(taxYearMax: Int): Seq[ChooseTaxYear] = valueMinYear(minYearDefault, taxYearMax)

  def valuesForEventType(eventType: EventType, taxYearMax: Int): Seq[ChooseTaxYear] = {
    val tempMinYear = eventType match {
      case Event23 => minYearEvent23
      case Event22 => minYearEvent22
      case _ => minYearDefault
    }
    valueMinYear(tempMinYear, taxYearMax)
  }

  private def valueMinYear(minYear: Int, taxYearMax: Int): Seq[ChooseTaxYear] = {
    (minYear to taxYearMax).reverse.map(year => ChooseTaxYear(year.toString))
  }

  def options(eventType: EventType, taxYearMax: Int)(implicit messages: Messages): Seq[RadioItem] = valuesForEventType(eventType, taxYearMax).zipWithIndex.map {
    case (value, index) =>
      val yearRangeOption = messages("chooseTaxYear.yearRangeRadio", value, (value.toString.toInt + 1).toString)
      RadioItem(
        content = Text(yearRangeOption),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit def enumerable(taxYearMax: Int): Enumerable[ChooseTaxYear] =
    Enumerable(values(taxYearMax).map(value => value.toString -> value): _*)
}
