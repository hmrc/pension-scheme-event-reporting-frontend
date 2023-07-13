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

package models

import play.api.libs.json.{Json, OFormat}

import java.util.UUID

case class UpscanFileReference(reference: String)

case class UpscanInitiateResponse(
                                   fileReference: UpscanFileReference,
                                   postTarget: String,
                                   formFields: Map[String, String]
                                 )

case class UploadId(value: String) extends AnyVal

object UploadId {
  def generate: UploadId = UploadId(UUID.randomUUID().toString)
}

case class FileUploadOutcomeResponse(fileName: Option[String], fileUploadStatus: FileUploadOutcomeStatus,
                                     downloadUrl: Option[String], reference: String, fileSize: Option[Long])

object FileUploadOutcomeResponse {
  implicit val format: OFormat[FileUploadOutcomeResponse] = Json.format[FileUploadOutcomeResponse]
}

sealed trait FileUploadOutcomeStatus

object FileUploadOutcomeStatus extends Enumerable.Implicits {

  case object IN_PROGRESS extends WithName("inProgress") with FileUploadOutcomeStatus

  case object SUCCESS extends WithName("success") with FileUploadOutcomeStatus

  case object FAILURE extends WithName("failure") with FileUploadOutcomeStatus

  private val values: List[FileUploadOutcomeStatus] = List(IN_PROGRESS, SUCCESS, FAILURE)

  implicit val enumerable: Enumerable[FileUploadOutcomeStatus] =
    Enumerable(values.map(v => v.toString -> v): _*)
}


