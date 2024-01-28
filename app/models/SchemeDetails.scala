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

import play.api.libs.json.{Format, Json, OFormat}

import java.time.LocalDate

case class PsaName(firstName: Option[String], middleName: Option[String], lastName: Option[String])

object PsaName {
  implicit val formats: OFormat[PsaName] = Json.format[PsaName]
}

case class AuthorisingPSA(
                           firstName: Option[String],
                           lastName: Option[String],
                           middleName: Option[String],
                           organisationOrPartnershipName: Option[String]
                         )

object AuthorisingPSA {
  implicit val formats: OFormat[AuthorisingPSA] = Json.format[AuthorisingPSA]
}

case class AuthorisingIndividual(
                                  firstName: String,
                                  lastName: String
                                )

object AuthorisingIndividual {
  implicit val formats: OFormat[AuthorisingIndividual] = Json.format[AuthorisingIndividual]
}
case class PsaDetails(id: String, organisationOrPartnershipName: Option[String], individual: Option[PsaName], relationshipDate: Option[String])

object PsaDetails {
  implicit val format: Format[PsaDetails] = Json.format[PsaDetails]
}
case class PspDetails(clientReference: Option[String]=None,
                      organisationOrPartnershipName: Option[String],
                      individual: Option[AuthorisingIndividual],
                      authorisingPSAID: String,
                      authorisingPSA: AuthorisingPSA,
                      relationshipStartDate: LocalDate,
                      id: String)

object PspDetails {
  implicit val format: Format[PspDetails] = Json.format[PspDetails]
}
case class PsaSchemeDetails(schemeName: String, pstr: String, schemeStatus: String, psaDetails: Seq[PsaDetails])
object PsaSchemeDetails {
  implicit val format: Format[PsaSchemeDetails] = Json.format[PsaSchemeDetails]
}

case class PspSchemeDetails(schemeName: String, pstr: String, schemeStatus: String, pspDetails: Option[PspDetails])
object PspSchemeDetails {
  implicit val format: Format[PspSchemeDetails] = Json.format[PspSchemeDetails]
}



