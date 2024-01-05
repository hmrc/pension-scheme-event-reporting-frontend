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

package models

import models.enumeration.AdministratorOrPractitioner
import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import play.api.libs.json.{Format, Json}

case class LoggedInUser(
                         externalId: String,
                         administratorOrPractitioner: AdministratorOrPractitioner,
                         psaIdOrPspId: String
                       ) {
  def idName: String = administratorOrPractitioner match {
    case Administrator => "psaId"
    case Practitioner => "pspId"
    case _ => "Unable to retrieve Id"
  }
}

object LoggedInUser {
  implicit val formats: Format[LoggedInUser] = Json.format[LoggedInUser]
}
