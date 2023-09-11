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

object DateHelper extends Mappings {

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
                                      )(implicit messages: Messages): (String, Mapping[LocalDate]) =
    field -> localDate(invalidKey).verifying(withinDateRange(date, outOfRangeKey): _*)

  def withinDateRange[T](input: T, errorKey: String): Seq[Constraint[LocalDate]] = input match {
    case int: Int =>
      Seq(
        minDate(LocalDate.of(int, 4, 6), errorKey, int.toString, (int + 1).toString),
        maxDate(LocalDate.of(int + 1, 4, 5), errorKey, int.toString, (int + 1).toString)
      )
    case localDate: LocalDate => ???
    case _ => throw new RuntimeException("withinDateRange does not support inputs of types other than Int or LocalDate")
  }
}
