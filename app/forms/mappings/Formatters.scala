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

package forms.mappings

import models.Enumerable
import org.apache.commons.lang3.StringUtils
import play.api.data.FormError
import play.api.data.format.Formatter

import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.{Currency, Locale}
import scala.util.control.Exception.nonFatalCatch
import scala.util.{Failure, Success, Try}

trait Formatters {

  private[mappings] val numericRegexp = """^-?(\-?)(\d*)(\.?)(\d*)$"""
  private[mappings] val decimal2DPRegexp = """^-?(\d*\.\d{2})$"""
  protected val decimalFormat = new DecimalFormat("0.00")

  val currencyFormatter = new DecimalFormat()
  currencyFormatter.setCurrency(Currency.getInstance(Locale.UK))
  currencyFormatter.setMinimumFractionDigits(2)

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  private[mappings] val optionalStringFormatter: Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      Right(
        data
          .get(key)
          .map(standardiseText)
          .filter(_.lengthCompare(0) > 0)
      )

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(StringUtils.EMPTY))
  }

  private def standardiseText(s: String): String = {
    s.replaceAll("""\s{1,}""", " ").trim
  }

  private[mappings] def stringFormatter(errorKey: String, args: Seq[String] = Seq.empty): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) => Right(s)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def booleanFormatter(requiredKey: String, invalidKey: String, args: Seq[String] = Seq.empty): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data).flatMap {
          case "true" => Right(true)
          case "false" => Right(false)
          case _ => Left(Seq(FormError(key, invalidKey, args)))
        }

      def unbind(key: String, value: Boolean) = Map(key -> value.toString)
    }

  private[mappings] def intFormatter(requiredKey: String, wholeNumberKey: String, nonNumericKey: String, args: Seq[String] = Seq.empty): Formatter[Int] =
    new Formatter[Int] {

      val decimalRegexp = """^-?(\d*\.\d*)$"""

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data).map(_.replace(",", StringUtils.EMPTY)).flatMap {
          case s if s.matches(decimalRegexp) =>
            Left(Seq(FormError(key, wholeNumberKey, args)))
          case s =>
            nonFatalCatch
              .either(s.toInt)
              .left.map(_ => Seq(FormError(key, nonNumericKey, args)))
        }

      override def unbind(key: String, value: Int) =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def bigDecimal2DPFormatter(nothingEnteredKey: String,
                                               notANumberKey: String,
                                               tooManyDecimalsKey: String,
                                               args: Seq[String] = Seq.empty): Formatter[BigDecimal] =
    new Formatter[BigDecimal] {

      private val baseFormatter = stringFormatter(nothingEnteredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", StringUtils.EMPTY).replace(" ", StringUtils.EMPTY))
          .map(_.replace("£", StringUtils.EMPTY).replace(" ", StringUtils.EMPTY))
          .flatMap {
          case stringWithNonNumericChars if !stringWithNonNumericChars.matches(numericRegexp) =>
            Left(Seq(FormError(key, notANumberKey, args)))
          case str =>
            val strAsBigDecimal = BigDecimal(str)
            if (strAsBigDecimal.scale > 2) {
              Left(Seq(FormError(key, tooManyDecimalsKey, args)))
            } else {
              Try(strAsBigDecimal) match {
                case Success(x) => Right(x)
                case Failure(_) => Left(Seq(FormError(key, notANumberKey, args)))
              }
            }
        }

      override def unbind(key: String, value: BigDecimal): Map[String, String] =
        baseFormatter.unbind(key, decimalFormat.format(value))
    }

  private[mappings] def optionBigDecimal2DPFormatter(invalidKey: String,
                                                     decimalKey: String,
                                                     args: Seq[String] = Seq.empty): Formatter[Option[BigDecimal]] =
    new Formatter[Option[BigDecimal]] {
      private val baseFormatter: Formatter[Option[String]] = optionalStringFormatter
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] =
        baseFormatter
          .bind(key, data)
          .map(_.map(_.replace(",", StringUtils.EMPTY).replace(" ", StringUtils.EMPTY)))
          .map(_.map(_.replace("£", StringUtils.EMPTY).replace(" ", StringUtils.EMPTY)))
          .flatMap {
          case s if s.isEmpty =>
            Right(None)
          case Some(stringWithNonNumericChars) if !stringWithNonNumericChars.matches(numericRegexp) =>
            Left(Seq(FormError(key, invalidKey, args)))
          case Some(str) =>
            val strAsBigDecimal = BigDecimal(str)
            if (strAsBigDecimal.scale > 2) {
              Left(Seq(FormError(key, decimalKey, args)))
            } else {
              Try(Option(strAsBigDecimal)) match {
                case Success(x) => Right(x)
                case Failure(_) => Left(Seq(FormError(key, invalidKey, args)))
              }
            }
          case None => Right(None)
        }

      override def unbind(key: String, value: Option[BigDecimal]): Map[String, String] =
        value match {
          case Some(str) =>
            baseFormatter.unbind(key, Some(decimalFormat.format(str)))
          case _ =>
            Map.empty
        }
    }

  private[mappings] def enumerableFormatter[A](requiredKey: String, invalidKey: String,
                                               args: Seq[String] = Seq.empty)(implicit ev: Enumerable[A]): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).flatMap {
          str =>
            if (str.isEmpty) {
              Left(Seq(FormError(key, invalidKey, args)))
            } else {
              ev.withName(str)
                .map(Right.apply)
                .getOrElse(Left(Seq(FormError(key, invalidKey, args))))
            }
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }
}
