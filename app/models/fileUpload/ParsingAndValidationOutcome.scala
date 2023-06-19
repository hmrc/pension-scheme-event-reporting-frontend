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
import play.api.libs.json._
import services.fileUpload.ParserValidationError

case class ParsingAndValidationOutcome(
                                        status: ParsingAndValidationOutcomeStatus,
                                        json: JsObject = Json.obj(),
                                        lessThanTen: Seq[ParserValidationError] = Nil,
                                        moreThanTen: Seq[String] = Nil,
                                        fileName: Option[String] = None
                                      )

object ParsingAndValidationOutcome {
  implicit val format: OFormat[ParsingAndValidationOutcome] = Json.format[ParsingAndValidationOutcome]

  private val readsErrorDetails: Reads[ParserValidationError] = (
    (JsPath \ "row").read[Int] and
      (JsPath \ "col").read[Int] and
      (JsPath \ "error").read[String] and
      (JsPath \ "columnName").read[String]
    )((row, col, error, columnName) =>
    ParserValidationError(
      row,
      col,
      error,
      columnName
    )
  )

  private def readsErrorsLessThan10(status: ParsingAndValidationOutcomeStatus): Reads[Option[Seq[ParserValidationError]]] = {
    status match {
      case ParsingAndValidationOutcomeStatus.ValidationErrorsLessThan10 =>
        JsPath.readNullable[Seq[ParserValidationError]](__.read(Reads.seq(readsErrorDetails)))
      case _ =>
        Reads.pure[Option[Seq[ParserValidationError]]](None)
    }
  }

  private def readsErrorsMoreThan10(status: ParsingAndValidationOutcomeStatus): Reads[Option[Seq[String]]] = {
    status match {
      case ParsingAndValidationOutcomeStatus.ValidationErrorsMoreThanOrEqual10 =>
        JsPath.readNullable[Seq[String]](__.read(Reads.seq[String]))
      case _ =>
        Reads.pure[Option[Seq[String]]](None)
    }
  }

  implicit val reads: Reads[ParsingAndValidationOutcome] = {
    (JsPath \ "status").read[ParsingAndValidationOutcomeStatus].flatMap {
      case status@ParsingAndValidationOutcomeStatus.ValidationErrorsLessThan10 =>
        (JsPath \ "errors").read[Option[Seq[ParserValidationError]]](readsErrorsLessThan10(status)).map { errors =>
          ParsingAndValidationOutcome(
            status = status,
            lessThanTen = errors.toSeq.flatten
          )
        }
      case status@ParsingAndValidationOutcomeStatus.ValidationErrorsMoreThanOrEqual10 =>
        (JsPath \ "errors").read[Option[Seq[String]]](readsErrorsMoreThan10(status)).map { errors =>
          ParsingAndValidationOutcome(
            status = status,
            moreThanTen = errors.toSeq.flatten
          )
        }
      case status@ParsingAndValidationOutcomeStatus.Success =>
        (JsPath \ "errors").read[Option[Seq[ParserValidationError]]](readsErrorsLessThan10(status)).map { errors =>
          ParsingAndValidationOutcome(
            status = status,
            lessThanTen = errors.toSeq.flatten
          )
        }
      case status@ParsingAndValidationOutcomeStatus.GeneralError =>
        (JsPath \ "errors").read[Option[Seq[ParserValidationError]]](readsErrorsLessThan10(status)).map { errors =>
          ParsingAndValidationOutcome(
            status = status,
            lessThanTen = errors.toSeq.flatten
          )
        }
      case _ =>
        throw new RuntimeException("Error")
    }
  }
}
