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
import models.FileUploadOutcomeStatus.{IN_PROGRESS, SUCCESS}
import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import models.enumeration.EventType.{Event1, Event2}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EventReportingUpscanFileDownloadAuditEventSpec extends AnyFlatSpec with Matchers {

  "EventReportingUpscanFileDownloadAuditEvent" should "output the correct map of data for Administrator" in {

    val fileUploadOutcomeResponse: FileUploadOutcomeResponse =
      FileUploadOutcomeResponse(Some("fileName"), IN_PROGRESS, Some("downloadUrl"), "reference", Some(400L))

    val event = EventReportingUpscanFileDownloadAuditEvent(
      Event1,
      psaOrPspId = "A2500001",
      "pstr",
      Administrator,
      fileUploadOutcomeResponse,
      "200",
      1000L
    )

    val expected: Map[String, String] = Map(
      "PensionSchemeAdministratorId" -> "A2500001",
      "PensionSchemeTaxReference" -> "pstr",
      "eventNumber" -> Event1.toString,
      "downloadStatus" -> "200",
      "downloadTimeInMilliSeconds" -> 1000L.toString,
      "reference" -> "reference"
    )

    event.auditType shouldBe "EventReportFileUpscanDownloadCheck"
    event.details shouldBe expected
  }

  "EventReportingUpscanFileDownloadAuditEvent" should "output the correct map of data for Practitioner" in {

    val fileUploadOutcomeResponse: FileUploadOutcomeResponse =
      FileUploadOutcomeResponse(Some("fileName"), SUCCESS, Some("downloadUrl"), "reference", Some(400L))

    val event = EventReportingUpscanFileDownloadAuditEvent(
      Event2,
      psaOrPspId = "2500001",
      "pstr",
      Practitioner,
      fileUploadOutcomeResponse,
      "200",
      1000L
    )

    val expected: Map[String, String] = Map(
      "PensionSchemePractitionerId" -> "2500001",
      "PensionSchemeTaxReference" -> "pstr",
      "eventNumber" -> Event2.toString,
      "downloadStatus" -> "200",
      "downloadTimeInMilliSeconds" -> 1000L.toString,
      "reference" -> "reference"
    )

    event.auditType shouldBe "EventReportFileUpscanDownloadCheck"
    event.details shouldBe expected
  }
}
