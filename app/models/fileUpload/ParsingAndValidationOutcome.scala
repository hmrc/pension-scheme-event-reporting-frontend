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

package models.fileUpload

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsArray, JsObject, JsPath, Json, OFormat, Reads, __}
import services.fileUpload.ParserValidationError

case class ParsingAndValidationOutcome(
                                        status: ParsingAndValidationOutcomeStatus,
                                        json: JsObject = Json.obj(),
                                        fileName: Option[String] = None
                                      )

object ParsingAndValidationOutcome {
  implicit val format: OFormat[ParsingAndValidationOutcome] = Json.format[ParsingAndValidationOutcome]

  private val readsDetails: Reads[ParserValidationError] = (
    (JsPath \ "json" \ "row").read[Int] and
      (JsPath \ "json" \ "col").read[Int] and
      (JsPath \ "json" \ "error").read[String] and
      (JsPath \ "json" \ "columnName").read[String]
    )((row, col, error, columnName) =>
    ParserValidationError(
      row,
      col,
      error,
      columnName
    )
  )
  private def readsTest(status: ParsingAndValidationOutcomeStatus): Reads[Option[JsArray]] = {
    status match {
      case ParsingAndValidationOutcomeStatus.Success =>
        Reads.pure[Option[JsArray]](None)
      case ParsingAndValidationOutcomeStatus.ValidationErrorsLessThan10 =>
        JsPath.readNullable[JsArray](__.read(Reads.seq(readsDetails)))
      case ParsingAndValidationOutcomeStatus.ValidationErrorsMoreThanOrEqual10 =>
        JsPath.readNullable[JsArray](__.read(Reads.seq(readsDetails)))
      case ParsingAndValidationOutcomeStatus.GeneralError =>
        Reads.pure[Option[JsArray]](None)
    }
  }

  implicit val reads: Reads[ParsingAndValidationOutcome] = {
   (JsPath \ "status").read[ParsingAndValidationOutcomeStatus].flatMap { status =>
      (JsPath \ "errors").read[Option[JsArray]](readsTest(status))
    }

  }
}
