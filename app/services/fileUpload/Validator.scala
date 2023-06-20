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

package services.fileUpload

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import models.UserAnswers
import models.common.MembersDetails
import models.enumeration.EventType
import models.fileUpload.FileUploadHeaders.MemberDetailsFieldNames
import org.apache.commons.lang3.StringUtils.EMPTY
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json._
import queries.Gettable
import services.fileUpload.ValidatorErrorMessages.HeaderInvalidOrFileIsEmpty

object ValidatorErrorMessages {
  val HeaderInvalidOrFileIsEmpty = "Header invalid or File is empty"
}

object Validator {
  type Result = Validated[Seq[ValidationError], Seq[CommitItem]]
  val FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty: ValidationError = ValidationError(0, 0, HeaderInvalidOrFileIsEmpty, EMPTY)
}

trait Validator {

  import Validator._

  protected val eventType: EventType

  protected val fieldNoFirstName = 0
  protected val fieldNoLastName = 1
  protected val fieldNoNino = 2

  protected def validHeader: String

  // scalastyle:off parameter.number

  protected def fieldValue(columns: Seq[String], fieldNo: Int): String =
    if (columns.isDefinedAt(fieldNo)) {
      columns(fieldNo)
    } else {
      ""
    }


  def validate(rows: Seq[Array[String]], userAnswers: UserAnswers)
              (implicit messages: Messages): Validated[Seq[ValidationError], UserAnswers] = {
    Invalid(Seq(FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty))
    rows.headOption match {
      case Some(row) if row.mkString(",").equalsIgnoreCase(validHeader) =>
        rows.size match {
          case n if n >= 2 => validateDataRows(rows)
            .map(_.foldLeft(userAnswers)((acc, ci) => acc.setOrException(ci.jsPath, ci.value)))
          case _ => Invalid(Seq(FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty))
        }
      case _ =>
        Invalid(Seq(FileLevelValidationErrorTypeHeaderInvalidOrFileEmpty))
    }
  }

  private def validateDataRows(rows: Seq[Array[String]])
                              (implicit messages: Messages): Result = {
    rows.zipWithIndex.foldLeft[Result](Valid(Nil)) {
      case (acc, Tuple2(_, 0)) => acc
      case (acc, Tuple2(row, index)) => Seq(acc, validateFields(index, row.toIndexedSeq)).combineAll
    }
  }

  protected def validateFields(index: Int,
                               columns: Seq[String])(implicit messages: Messages): Result

  protected def memberDetailsValidation(index: Int, columns: Seq[String],
                                        memberDetailsForm: Form[MembersDetails])(implicit messages: Messages): Validated[Seq[ValidationError], MembersDetails] = {
    val fields = Seq(
      Field(MemberDetailsFieldNames.firstName, fieldValue(columns, fieldNoFirstName), MemberDetailsFieldNames.firstName, 0),
      Field(MemberDetailsFieldNames.lastName, fieldValue(columns, fieldNoLastName), MemberDetailsFieldNames.lastName, 1),
      Field(MemberDetailsFieldNames.nino, fieldValue(columns, fieldNoNino), MemberDetailsFieldNames.nino, 2)
    )
    val toMap = Field.seqToMap(fields)

    val bind = memberDetailsForm.bind(toMap)
    bind.fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  protected final def errorsFromForm[A](formWithErrors: Form[A], fields: Seq[Field], index: Int)(implicit messages: Messages): Seq[ValidationError] = {
    for {
      formError <- formWithErrors.errors
      field <- fields.find(_.getFormValidationFullFieldName == formError.key)
    }
    yield {
      ValidationError(index, field.columnNo, messages(formError.message), field.columnName)
    }
  }

  protected final def createCommitItem[A](index: Int, page: Int => Gettable[_])(implicit writes: Writes[A]): A => CommitItem =
    a => CommitItem(page(index - 1).path, Json.toJson(a))

  protected def resultFromFormValidationResult[A](formValidationResult: Validated[Seq[ValidationError], A],
                                                  generateCommitItem: A => CommitItem): Result = {
    formValidationResult match {
      case Invalid(resultAErrors) => Invalid(resultAErrors)
      case Valid(resultAObject) => Valid(Seq(generateCommitItem(resultAObject)))
    }
  }

  protected def get[A](r: Result)(implicit reads: Reads[A]): Option[A] =
    r.toOption.flatMap(_.headOption.flatMap(_.value.asOpt[A]))

  protected def getOrElse[A](r: Result, default: A)(implicit reads: Reads[A]): A =
    r.toOption.flatMap(_.headOption.flatMap(_.value.asOpt[A])) match {
      case None => default
      case Some(a) => a
    }

  protected final def splitDayMonthYear(date: String): ParsedDate = {
    date.split("/").toSeq match {
      case Seq(d, m, y) => ParsedDate(d, m, y)
      case Seq(d, m) => ParsedDate(d, m, EMPTY)
      case Seq(d) => ParsedDate(d, EMPTY, EMPTY)
      case _ => ParsedDate(EMPTY, EMPTY, EMPTY)
    }
  }

  protected final def stringToBoolean(s: String): String =
    s.toLowerCase match {
      case "yes" => "true"
      case "no" => "false"
      case l => l
    }
}

case class ValidationError(row: Int, col: Int, error: String, columnName: String = EMPTY)

object ValidationError {
  implicit val format: OFormat[ValidationError] = Json.format[ValidationError]

  //implicit val Writes: OWrites[Seq[ValidationError]] = Json.writes[Seq[ValidationError]]

//    implicit val writes: Writes[Seq[ValidationError]] = {
//      Writes(value =>
//        Json.obj(
//          "row" -> value.row,
//          "col" -> value.col,
//          "error" -> value.error,
//          "columnName" -> value.columnName
//        )
//      )
//    }
}

protected case class CommitItem(jsPath: JsPath, value: JsValue)

protected case class Field(formValidationFieldName: String,
                           fieldValue: String,
                           columnName: String,
                           columnNo: Int,
                           private val formValidationFullFieldName: Option[String] = None
                          ) {
  def getFormValidationFullFieldName: String = formValidationFullFieldName match {
    case Some(v) => v
    case _ => formValidationFieldName
  }
}

protected case class ParsedDate(day: String, month: String, year: String)

protected object Field {
  def seqToMap(s: Seq[Field]): Map[String, String] = {
    s.map { f =>
      f.formValidationFieldName -> f.fieldValue
    }.toMap
  }
}