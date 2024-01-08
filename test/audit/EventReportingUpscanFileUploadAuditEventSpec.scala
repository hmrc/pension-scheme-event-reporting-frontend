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
import models.FileUploadOutcomeStatus.SUCCESS
import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import models.enumeration.EventType.{Event1, Event2}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EventReportingUpscanFileUploadAuditEventSpec extends AnyFlatSpec with Matchers {

  "EventReportingUpscanFileUploadAudit" should "output the correct map of data for Administrator" in {

    val fileUploadOutcomeResponse: FileUploadOutcomeResponse =
      FileUploadOutcomeResponse(Some("fileName"), SUCCESS, Some("downloadUrl"), "reference", Some(400L))

    val event = EventReportingUpscanFileUploadAuditEvent(
      Event1,
      psaOrPspId = "A2500001",
      "pstr",
      Administrator,
      Right(fileUploadOutcomeResponse),
      1000L
    )

    val expected: Map[String, String] = Map(
      "PensionSchemeAdministratorId" -> "A2500001",
      "PensionSchemeTaxReference" -> "pstr",
      "eventNumber" -> Event1.toString,
      "uploadTimeInMillSeconds" -> 1000L.toString,
      "fileSize" -> fileUploadOutcomeResponse.fileSize.get.toString,
      "uploadStatus" -> SUCCESS.toString,
      "reference" -> fileUploadOutcomeResponse.reference
    )

    event.auditType shouldBe "EventReportFileUpscanUploadCheck"
    event.details shouldBe expected
  }

  "EventReportingUpscanFileDownloadAuditEvent" should "output the correct map of error data for Practitioner" in {

    val event = EventReportingUpscanFileUploadAuditEvent(
      Event2,
      psaOrPspId = "A2500001",
      "pstr",
      Practitioner,
      Left("error"),
      1000L
    )

    val expected: Map[String, String] = Map(
      "PensionSchemePractitionerId" -> "A2500001",
      "PensionSchemeTaxReference" -> "pstr",
      "eventNumber" -> Event2.toString,
      "uploadTimeInMillSeconds" -> 1000L.toString,
      "uploadStatus" -> "Failed",
      "failureReason" -> "Service Unavailable",
      "failureDetail" -> "error"
    )

    event.auditType shouldBe "EventReportFileUpscanUploadCheck"
    event.details shouldBe expected
  }
}
