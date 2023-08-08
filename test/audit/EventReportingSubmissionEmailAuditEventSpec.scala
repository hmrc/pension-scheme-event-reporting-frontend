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

package audit

import connectors.EmailSent
import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EventReportingSubmissionEmailAuditEventSpec extends AnyFlatSpec with Matchers {

  "EventReportingEmailAuditEvent" should "output the correct map of data for PSA" in {

    val event = EventReportingSubmissionEmailAuditEvent(
      psaOrPspId = "A2500001",
      pstr = "test-pstr",
      Administrator,
      emailAddress = "test@test.com",
      reportVersion = "1",
      EmailSent
    )

    val expected: Map[String, String] = Map(
      "emailAddress" -> "test@test.com",
      "submittedBy" -> Administrator.toString,
      "PensionSchemeAdministratorId" -> "A2500001",
      "reportVersion" -> "1",
      "event" -> "EmailSent",
      "PensionSchemeTaxReference" -> "test-pstr"
    )

    event.auditType shouldBe "EventReportingEmailEvent"
    event.details shouldBe expected
  }

  "EventReportingEmailAuditEvent" should "output the correct map of data for PSP" in {

    val event = EventReportingSubmissionEmailAuditEvent(
      psaOrPspId = "2500001",
      pstr = "test-pstr",
      Practitioner,
      emailAddress = "test@test.com",
      reportVersion = "1",
      EmailSent
    )

    val expected: Map[String, String] = Map(
      "emailAddress" -> "test@test.com",
      "submittedBy" -> Practitioner.toString,
      "PensionSchemePractitionerId" -> "2500001",
      "reportVersion" -> "1",
      "event" -> "EmailSent",
      "PensionSchemeTaxReference" -> "test-pstr"
    )

    event.auditType shouldBe "EventReportingEmailEvent"
    event.details shouldBe expected
  }
}
