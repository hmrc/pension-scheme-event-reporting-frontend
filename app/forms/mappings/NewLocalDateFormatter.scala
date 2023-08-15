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

package forms.mappings

import helpers.DateHelper
import models.TaxYearValidationDetail
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.i18n.Messages

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

private[mappings] class NewLocalDateFormatter(
                                               invalidKey: String,
                                               taxYearValidationDetail: Option[TaxYearValidationDetail] = None,
                                               args: Seq[String] = Seq.empty
                                             )(implicit messages: Messages) extends Formatter[LocalDate] with Formatters {

  private val fieldKeys: List[String] = List("day", "month", "year")

  def tryLocalDate(input: (Int, Int, Int)): Either[Seq[FormError], LocalDate] = {
    Try(LocalDate.of(input._3, input._2, input._1)) match {
      case Success(date) => Right(date)
      case Failure(exception) =>
        val errorMessage = exception.getMessage
         errorMessage match {
        case _ =>
            Left(Seq(FormError(invalidKey, messages(erroneousDateKey(errorMessage)))))
      }
    }
  }

  private val erroneousDateKey: String => String = {
    case errorMessage@invalidDay if errorMessage.contains("DayOfMonth") => "genericDate.error.invalid.day"
    case errorMessage@invalidMonth if errorMessage.contains("MonthOfYear") => "genericDate.error.invalid.month"
    case errorMessage@invalidYear if errorMessage.contains("Year") => "genericDate.error.invalid.year"
    case _ => invalidKey
  }

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val int = intFormatter(
      requiredKey = invalidKey,
      wholeNumberKey = invalidKey,
      nonNumericKey = invalidKey,
      args
    )

    for {
      day <- int.bind(s"$key.day", data)
      month <- int.bind(s"$key.month", data)
      year <- int.bind(s"$key.year", data)
      tryDate <- tryLocalDate(day, month, year)
    } yield tryDate
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val fields = fieldKeys.map {
      field =>
        field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 3 =>
        val formattedDate = formatDate(key, data).left.map {
          _.map(_.copy(key = key, args = args))
        }
        formattedDate match {
          case errors@Left(_) => errors
          case rightDate@Right(d) =>
            taxYearValidationDetail match {
              case None => rightDate
              case Some(TaxYearValidationDetail(invalidKey, taxYear)) =>
                val taxYearForDate = DateHelper.extractTaxYear(d)
                if (taxYearForDate == taxYear) {
                  rightDate
                } else {
                  Left(List(FormError(key, invalidKey, Seq(taxYear.toString, (taxYear + 1).toString))))
                }
            }
        }
      case 2 =>
        Left(List(FormError(key, s"${messages("genericDate.error.invalid.missingInformation")} ${missingFields.head}", args)))
      case 1 =>
        val missingFieldsString = s"${missingFields.head} and ${missingFields.tail.head}"
        Left(List(FormError(key, s"${messages("genericDate.error.invalid.missingInformation")} $missingFieldsString", args)))
      case _ =>
        Left(List(FormError(key, "genericDate.error.invalid.allFieldsMissing", args)))
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.day" -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year" -> value.getYear.toString
    )
}
