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

package models.fileUpload

import models.{Enumerable, WithName}

sealed trait ParsingAndValidationOutcomeStatus

object ParsingAndValidationOutcomeStatus extends Enumerable.Implicits {

  case object Success extends WithName("Success") with ParsingAndValidationOutcomeStatus

  case object GeneralError extends WithName("GeneralError") with ParsingAndValidationOutcomeStatus

  case object IncorrectHeadersOrEmptyFile extends WithName("IncorrectHeadersOrEmptyFile") with ParsingAndValidationOutcomeStatus

  case object ValidationErrorsLessThan10 extends WithName("ValidationErrorsLess10") with ParsingAndValidationOutcomeStatus

  case object ValidationErrorsMoreThanOrEqual10 extends WithName("ValidationErrorsMoreThanOrEqualTo10") with ParsingAndValidationOutcomeStatus

  val values: Seq[ParsingAndValidationOutcomeStatus] = Seq(
    Success,
    GeneralError,
    ValidationErrorsLessThan10,
    ValidationErrorsMoreThanOrEqual10,
    IncorrectHeadersOrEmptyFile
  )

  implicit val enumerable: Enumerable[ParsingAndValidationOutcomeStatus] =
    Enumerable(values.map(v => v.toString -> v)*)
}
