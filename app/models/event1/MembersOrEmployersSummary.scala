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

package models.event1

import models.enumeration.EventType.Event1
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import pages.event1.WhoReceivedUnauthPaymentPage
import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Format, JsPath, Json, Reads}

case class MembersOrEmployersSummary(name: String, unauthorisedPaymentValue: BigDecimal, memberStatus: Option[String], nino: Option[String] = None, companyNumber: Option[String] = None)

object MembersOrEmployersSummary {
  implicit lazy val formats: Format[MembersOrEmployersSummary] = Json.format[MembersOrEmployersSummary]

  val readsMemberOrEmployerValue: Reads[BigDecimal] =
    (JsPath \ "paymentValueAndDate" \ "paymentValue").readNullable[BigDecimal]
      .map(_.getOrElse(BigDecimal(0)))

  private def readsMemberSummary(implicit messages: Messages): Reads[MembersOrEmployersSummary] =
    (
      (JsPath \ "membersDetails" \ "firstName").readNullable[String] and
        (JsPath \ "membersDetails" \ "lastName").readNullable[String] and
        (JsPath \ "membersDetails" \ "nino").readNullable[String] and
        readsMemberOrEmployerValue and
        (JsPath \ "memberStatus").readNullable[String]
      ) (
      (firstName, lastName, nino, paymentValue, memberStatus) => {
        (firstName, lastName, paymentValue) match {
          case (Some(fn), Some(ln), _) =>  MembersOrEmployersSummary(fn + " " + ln, paymentValue, memberStatus, nino)
          case (None, Some(ln), _) =>  MembersOrEmployersSummary(ln, paymentValue, memberStatus, nino)
          case (Some(fn), None, _) =>  MembersOrEmployersSummary(fn, paymentValue, memberStatus, nino)
          case (None, None, _) =>  MembersOrEmployersSummary(messages("site.notEntered"), paymentValue, memberStatus, nino)
        }
      }
    )

  private def readsEmployerSummary(implicit messages: Messages): Reads[MembersOrEmployersSummary] =
    (
      (JsPath \ s"event${Event1.toString}" \ "companyDetails" \ "companyName").readNullable[String] and
        (JsPath \ s"event${Event1.toString}" \ "companyDetails" \ "companyNumber").readNullable[String] and
        readsMemberOrEmployerValue and
        (JsPath \ "memberStatus").readNullable[String]
      ) (
      (companyName, companyNumber, paymentValue, memberStatus) => {
        (companyName, paymentValue) match {
          case (Some(cn), _) =>  MembersOrEmployersSummary(cn, paymentValue, memberStatus, companyNumber = companyNumber)
          case (None, _) =>  MembersOrEmployersSummary(messages("site.notEntered"), paymentValue, memberStatus)
        }
      }
    )

  def readsMemberOrEmployer(implicit messages: Messages): Reads[MembersOrEmployersSummary] = {
    (JsPath \ WhoReceivedUnauthPaymentPage.toString).readNullable[String].flatMap {
      case Some(Member.toString) => readsMemberSummary
      case Some(Employer.toString) => readsEmployerSummary
      case None => Reads.pure[MembersOrEmployersSummary](MembersOrEmployersSummary(messages("site.notEntered"), BigDecimal(0.00), Some("Any")))
      case _ => throw new RuntimeException("Failed reads. Neither Member or Employer")
    }
  }
}
