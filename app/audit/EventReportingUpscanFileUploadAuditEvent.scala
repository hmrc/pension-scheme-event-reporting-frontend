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

package audit

import models.FileUploadOutcomeResponse
import models.enumeration.{AdministratorOrPractitioner, EventType}

case class EventReportingUpscanFileUploadAuditEvent(eventType: EventType,
                                                    psaOrPspId: String,
                                                    pstr: String,
                                                    schemeAdministratorType: AdministratorOrPractitioner,
                                                    outcome: Either[String, FileUploadOutcomeResponse],
                                                    uploadTimeInMilliSeconds: Long
                                                        ) extends AuditEvent {

  override def auditType: String = "EventReportFileUpscanUploadCheck"

  override def details: Map[String, String] = {

    val psaOrPspIdJson = schemeAdministratorType match {
      case AdministratorOrPractitioner.Administrator =>
        Map("PensionSchemeAdministratorId" -> psaOrPspId)
      case _ => Map("PensionSchemePractitionerId" -> psaOrPspId)
    }

    val detailMap = outcome match {
      case Left(error) =>
        Map(
          "uploadStatus" -> "Failed",
          "failureReason" -> "Service Unavailable",
          "failureDetail" -> error
        )

      case Right(fileUploadOutcomeResponse) =>
        val fileSizeMap = fileUploadOutcomeResponse.fileSize match {
          case Some(v) =>
            Map(
              "fileSize" -> v.toString
            )
          case _ => Map.empty
        }

        Map("uploadStatus" -> fileUploadOutcomeResponse.fileUploadStatus.toString) ++ fileSizeMap ++ Map("reference" -> fileUploadOutcomeResponse.reference)
    }

    psaOrPspIdJson ++
      Map(
        "PensionSchemeTaxReference" -> pstr,
        "eventNumber" -> eventType.toString,
        "uploadTimeInMillSeconds" -> uploadTimeInMilliSeconds.toString
      ) ++ detailMap
  }
}