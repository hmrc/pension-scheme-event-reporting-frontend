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

package utils

import forms.mappings.Mappings
import play.api.data.Mapping
import play.api.data.validation.Constraint
import play.api.i18n.Messages

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZonedDateTime}
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

import scala.reflect.runtime.universe._
import scala.reflect.runtime.universe.TypeTag

object DateHelper extends Mappings {

  private val april = 4
  private val taxYearOpenDay = 6
  private val taxYearCloseDay = 5

  val dateFormatterDMY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  private val dateFormatterSubmittedDate: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.UK)

  def formatDateDMY(date: LocalDate): String = date.format(dateFormatterDMY)

  private val mockDate: AtomicReference[Option[LocalDate]] = new AtomicReference(None)

  def today: LocalDate = mockDate.get().getOrElse(LocalDate.now())

  def setDate(date: Option[LocalDate]): Unit = mockDate.set(date)

  def overriddenDate: Option[LocalDate] = mockDate.get()

  def formatSubmittedDate(dateTime: ZonedDateTime): String = {
    val str = dateFormatterSubmittedDate.format(dateTime)
    val suffix = str.takeRight(2).toLowerCase
    val prefix = str.take(str.length - 2)
    prefix + suffix
  }

  def localDateMappingWithDateRange[T](
                                        field: String = "value",
                                        invalidKey: String = "genericDate.error.invalid",
                                        date: T,
                                        outOfRangeKey: String
                                      )(implicit messages: Messages, tag: TypeTag[T]): (String, Mapping[LocalDate]) =
    field -> localDate(invalidKey).verifying(firstError(withinDateRange(date, outOfRangeKey): _*))

  def withinDateRange[T](input: T, errorKey: String)(implicit messages: Messages, tag: TypeTag[T]): Seq[Constraint[LocalDate]] = tag.tpe match {
    case int if int =:= typeOf[Int] =>
      val intValue = input.asInstanceOf[Int]
      Seq(
        yearHas4Digits("genericDate.error.invalid.year"),
        minDate(LocalDate.of(intValue, april, taxYearOpenDay), errorKey, intValue.toString, (intValue + 1).toString),
        maxDate(LocalDate.of(intValue + 1, april, taxYearCloseDay), errorKey, intValue.toString, (intValue + 1).toString)
      )
    case localDates if localDates =:= typeOf[(LocalDate, LocalDate)]  =>
      val tupleValue = input.asInstanceOf[(LocalDate, LocalDate)]
      Seq(
        yearHas4Digits("genericDate.error.invalid.year"),
        minDate(tupleValue._1, messages(errorKey, formatDateDMY(tupleValue._1), formatDateDMY(tupleValue._2))),
        maxDate(tupleValue._2, messages(errorKey, formatDateDMY(tupleValue._1), formatDateDMY(tupleValue._2)))
      )
    case intAndLocalDate if intAndLocalDate =:= typeOf[(Int, LocalDate)]  =>
      val tupleValue = input.asInstanceOf[(Int, LocalDate)]
      Seq(
        yearHas4Digits("genericDate.error.invalid.year"),
        minDate(LocalDate.of(tupleValue._1, april, taxYearOpenDay), errorKey, tupleValue._1.toString, (tupleValue._1 + 1).toString),
        maxDate(LocalDate.of(tupleValue._1 + 1, april, taxYearCloseDay), errorKey, tupleValue._1.toString, (tupleValue._1 + 1).toString),
        isNotBeforeOpenDate(tupleValue._2, "schemeWindUpDate.error.beforeOpenDate", formatDateDMY(tupleValue._2))
      )
    case _ =>
      throw new ClassCastException("withinDateRange does not support inputs of types other than Int, (LocalDate, LocalDate), or (Int, LocalDate)")
  }
}
