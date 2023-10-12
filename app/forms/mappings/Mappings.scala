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

import models.{Enumerable, TaxYearValidationDetail}
import play.api.data.FieldMapping
import play.api.data.Forms.of
import play.api.i18n.Messages

import java.time.LocalDate

trait Mappings extends Formatters with Constraints {

  protected def optionalText(): FieldMapping[Option[String]] =
    of(optionalStringFormatter)

  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def int(requiredKey: String = "error.required",
                    wholeNumberKey: String = "error.wholeNumber",
                    nonNumericKey: String = "error.nonNumeric",
                    args: Seq[String] = Seq.empty): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey, args))

  protected def bigDecimal2DP(
                        nothingEnteredKey: String = "error.nothingEntered",
                        notANumberKey: String = "error.notANumber",
                        tooManyDecimalsKey: String = "error.tooManyDecimals",
                        args: Seq[String] = Seq.empty): FieldMapping[BigDecimal] =
    of(bigDecimal2DPFormatter(nothingEnteredKey, notANumberKey, tooManyDecimalsKey, args))


  protected def optionBigDecimal2DP(invalidKey: String = "error.invalid",
                                    decimalKey: String = "error.decimal"
                                   ): FieldMapping[Option[BigDecimal]] =
    of(optionBigDecimal2DPFormatter(invalidKey, decimalKey))

  protected def boolean(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean",
                        args: Seq[String] = Seq.empty): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))

  protected def enumerable[A](requiredKey: String = "error.required",
                              invalidKey: String = "error.invalid",
                              args: Seq[String] = Seq.empty)(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))

  protected def enumerable2024[A](requiredKey: String = "error.required",
                              invalidKey: String = "error.invalid",
                              args: Seq[String] = Seq.empty, ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter2024[A](requiredKey, invalidKey, args, ev))

  protected def localDate(
                              invalidKey: String,
                              taxYearValidationDetail: Option[TaxYearValidationDetail] = None,
                              args: Seq[String] = Seq.empty
                            )(implicit messages: Messages): FieldMapping[LocalDate] =

    of(new LocalDateFormatter(invalidKey, taxYearValidationDetail, args))
}
