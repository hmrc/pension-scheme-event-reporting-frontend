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

import models.TaxYear
import models.enumeration.EventType

case class StartNewERAuditEvent(
                               psaIdentifier: String,
                               pstr: String,
                               taxYear: TaxYear,
                               eventNumber: EventType,
                               reportVersion: String
                             ) extends AuditEvent {
  override def auditType: String = "EventReportingStart"

  override def details: Map[String, String] = {
    Map(
      "PensionSchemeAdministratorOrPensionSchemePractitionerId" -> psaIdentifier,
      "PensionSchemeTaxReference" -> pstr,
      "taxYear" -> s"${taxYear.startYear}-${taxYear.endYear}",
      "eventNumber" -> eventNumber.toString,
      "reportVersion" -> reportVersion
    )
  }
}
