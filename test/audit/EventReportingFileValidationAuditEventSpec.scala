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

import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import models.enumeration.EventType.{Event1, Event2}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EventReportingFileValidationAuditEventSpec extends AnyFlatSpec with Matchers {

  "EventReportingFileValidationAudit" should "output the correct map of data for Administrator" in {

    val event = EventReportingFileValidationAuditEvent(
      Event1,
      psaOrPspId = "A2500001",
      "pstr",
      Administrator,
      1000L,
      5,
      false,
      Some("failure-reason"),
      5,
      Some("validation-failure")
    )

    val expected: Map[String, String] = Map(
      "PensionSchemeAdministratorId" -> "A2500001",
      "PensionSchemeTaxReference" -> "pstr",
      "numberOfEntries" -> 5.toString,
      "eventNumber" -> Event1.toString,
      "validationCheckStatus" -> "Failure",
      "fileValidationTimeInSeconds" -> 1000L.toString,
      "numberOfFailures" -> 5.toString,
      "failureReason" -> "failure-reason",
      "validationFailureContent" -> "validation-failure"
    )

    event.auditType shouldBe "EventReportFileValidationCheck"
    event.details shouldBe expected
  }

  "EventReportingFileValidationAudit" should "output the correct map of error data for Practitioner" in {

    val event = EventReportingFileValidationAuditEvent(
      Event2,
      psaOrPspId = "A2500001",
      "pstr",
      Practitioner,
      1000L,
      5,
      true,
      None,
      0,
      None
    )

    val expected: Map[String, String] = Map(
      "PensionSchemePractitionerId" -> "A2500001",
      "PensionSchemeTaxReference" -> "pstr",
      "numberOfEntries" -> 5.toString,
      "eventNumber" -> Event2.toString,
      "validationCheckStatus" -> "Success",
      "fileValidationTimeInSeconds" -> 1000L.toString,
      "numberOfFailures" -> 0.toString
    )

    event.auditType shouldBe "EventReportFileValidationCheck"
    event.details shouldBe expected
  }
}
