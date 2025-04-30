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

package models

import config.FrontendAppConfig
import utils.DateHelper._

import java.time.{LocalDate, Month}
import scala.language.implicitConversions

sealed trait QuarterType {
  def startMonth: Int
  def endMonth: Int
  def startDay: Int = 1
  def endDay: Int
}

trait CommonQuarters {
  def currentYear: Int = today.getYear

  case object Q1 extends WithName("q1") with QuarterType {
    override def startMonth: Int = Month.JANUARY.getValue

    override def endMonth: Int = Month.MARCH.getValue

    override def endDay: Int = Month.MARCH.maxLength()
  }

  case object Q2 extends WithName("q2") with QuarterType {
    override def startMonth: Int = Month.APRIL.getValue

    override def endMonth: Int = Month.JUNE.getValue

    override def endDay: Int = Month.JUNE.maxLength()
  }

  case object Q3 extends WithName("q3") with QuarterType {
    override def startMonth: Int = Month.JULY.getValue

    override def endMonth: Int = Month.SEPTEMBER.getValue

    override def endDay: Int = Month.SEPTEMBER.maxLength()
  }

  case object Q4 extends WithName("q4") with QuarterType {
    override def startMonth: Int = Month.OCTOBER.getValue

    override def endMonth: Int = Month.DECEMBER.getValue

    override def endDay: Int = Month.DECEMBER.maxLength()
  }

  def getCurrentYearQuarters(implicit config: FrontendAppConfig): Seq[QuarterType] = {
    val quartersCY = today.getMonthValue match {
      case i if i > 9 => Seq(Q1, Q2, Q3, Q4)
      case i if i > 6 => Seq(Q1, Q2, Q3)
      case i if i > 3 => Seq(Q1, Q2)
      case _ => Seq(Q1)
    }

    if (currentYear == config.minimumYear) {
      quartersCY.filter(_ != Q1)
    }
    else {
      quartersCY
    }
  }

  def getQuarter(quarter: QuarterType, year: Int): AFTQuarter = {
    AFTQuarter(LocalDate.of(year, quarter.startMonth, quarter.startDay),
      LocalDate.of(year, quarter.endMonth, quarter.endDay))
  }

  def getStartDate(quarter: QuarterType, year: Int): LocalDate =
    LocalDate.of(year, quarter.startMonth, quarter.startDay)

  def getQuarter(startDate: LocalDate): AFTQuarter =
    getQuarter(getQuartersFromDate(startDate), startDate.getYear)

  def getQuartersFromDate(date: LocalDate): QuarterType =
    date.getMonthValue match {
      case i if i <= 3 => Q1
      case i if i <= 6 => Q2
      case i if i <= 9 => Q3
      case _ => Q4
    }
}

object Quarters extends CommonQuarters with Enumerable.Implicits {

  implicit def enumerable(quarters: Seq[AFTQuarter]): Enumerable[AFTQuarter] =
    Enumerable(quarters.map(v => v.toString -> v)*)

}
