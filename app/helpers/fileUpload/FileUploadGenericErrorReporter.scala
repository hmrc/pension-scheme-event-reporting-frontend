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

package helpers.fileUpload

import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event6}
import models.fileUpload.FileUploadHeaders._
import services.fileUpload.ValidationError

object FileUploadGenericErrorReporter {

  import models.fileUpload.FileUploadHeaders.MemberDetailsFieldNames._

  case class ColumnAndError(columnName: String, errorDescription: String)

  type ErrorReport = Seq[String]
  type ColumnAndErrorMap = Map[String, String]

  private val commonColumnAndErrorMessageMap = Map(
    firstName -> "fileUpload.memberDetails.generic.error.firstName",
    lastName -> "fileUpload.memberDetails.generic.error.lastName",
    nino -> "fileUpload.memberDetails.generic.error.nino"
  )


  private def eventHeader(eventType: EventType) = {
    eventType match {
      case Event6 =>
        commonColumnAndErrorMessageMap ++
          Map(
            Event6FieldNames.typeOfProtection -> "fileUpload.typeOfProtection.generic.error",
            Event6FieldNames.typeOfProtectionReference -> "fileUpload.typeOfProtectionReference.generic.error",
            Event6FieldNames.lumpSumAmount -> "fileUpload.lumpSumAmount.generic.error",
            Event6FieldNames.lumpSumDate -> "fileUpload.lumpSumDate.generic.error"
          )
      case Event22 =>
        commonColumnAndErrorMessageMap ++
          Map(
            Event22FieldNames.taxYear -> "fileUpload.taxYear.generic.error",
            Event22FieldNames.totalAmounts -> "fileUpload.totalAmounts.generic.error"
          )
      case _ =>
        commonColumnAndErrorMessageMap ++
          Map(
            Event23FieldNames.taxYear -> "fileUpload.taxYear.generic.error",
            Event23FieldNames.totalAmounts -> "fileUpload.totalAmounts.generic.error"
          )
    }

  }

  private def getColumnsAndErrorMap(eventType: EventType): ColumnAndErrorMap = eventType match {
    case EventType.Event6 => eventHeader(eventType)
    case EventType.Event22 => eventHeader(eventType)
    case EventType.Event23 => eventHeader(eventType)
    case _ => throw new RuntimeException("Invalid event type")
  }

  def generateGenericErrorReport(errors: Seq[ValidationError], eventType: EventType): ErrorReport = {
    val eventTypeHeaderMap = getColumnsAndErrorMap(eventType)
    val columns = errors.map(_.columnName).intersect(eventTypeHeaderMap.keySet.toSeq)
    columns.map(col => eventTypeHeaderMap.apply(col))
  }

}
