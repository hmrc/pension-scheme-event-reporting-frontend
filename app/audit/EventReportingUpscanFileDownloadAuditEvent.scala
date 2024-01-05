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

case class EventReportingUpscanFileDownloadAuditEvent(eventType: EventType,
                                                      psaOrPspId: String,
                                                      pstr: String,
                                                      schemeAdministratorType: AdministratorOrPractitioner,
                                                      fileUploadOutcomeResponse: FileUploadOutcomeResponse,
                                                      downloadStatus: String,
                                                      downloadTimeInMilliSeconds: Long
                                                     ) extends AuditEvent {

  override def auditType: String = "EventReportFileUpscanDownloadCheck"

  override def details: Map[String, String] = {

    val psaOrPspIdJson = schemeAdministratorType match {
      case AdministratorOrPractitioner.Administrator =>
        Map("PensionSchemeAdministratorId" -> psaOrPspId)
      case _ => Map("PensionSchemePractitionerId" -> psaOrPspId)
    }

    psaOrPspIdJson ++
      Map(
        "PensionSchemeTaxReference" -> pstr,
        "eventNumber" -> eventType.toString,
        "downloadStatus" -> downloadStatus,
        "downloadTimeInMilliSeconds" -> downloadTimeInMilliSeconds.toString,
        "reference" -> fileUploadOutcomeResponse.reference
      )
  }
}