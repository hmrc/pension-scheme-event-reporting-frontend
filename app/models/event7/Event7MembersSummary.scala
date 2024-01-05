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

package models.event7

import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Format, JsPath, Json, Reads}

case class Event7MembersSummary(
                                 name: String,
                                 PaymentValue: BigDecimal,
                                 PaymentValueTwo: BigDecimal,
                                 nINumber: String,
                                 memberStatus: Option[String]
                               )

object Event7MembersSummary {
  implicit lazy val formats: Format[Event7MembersSummary] = Json.format[Event7MembersSummary]

  private def readsNINumber(implicit messages: Messages): Reads[String] =
    (JsPath \ "membersDetails" \ "nino").readNullable[String]
      .map(_.getOrElse(messages("site.notEntered")))

  private def readsLumpSumValue: Reads[BigDecimal] =
    (JsPath \ "lumpSumAmount").readNullable[BigDecimal]
      .map(_.getOrElse(BigDecimal(0)))

  private def readsCrystallisedValue: Reads[BigDecimal] =
    (JsPath \ "crystallisedAmount").readNullable[BigDecimal]
      .map(_.getOrElse(BigDecimal(0)))

  private def readsMemberSummary(implicit messages: Messages): Reads[Event7MembersSummary] =
    (
      (JsPath \ "membersDetails" \ "firstName").readNullable[String] and
        (JsPath \ "membersDetails" \ "lastName").readNullable[String] and
        (JsPath  \ "memberStatus").readNullable[String] and
        readsLumpSumValue and
        readsCrystallisedValue and
        readsNINumber
      ) (
      (firstName, lastName, memberStatus, paymentValue, paymentValueTwo, nINumber) => {
        (firstName, lastName, memberStatus, paymentValue, paymentValueTwo, nINumber) match {
          case (Some(fn), Some(ln), _, _, _, _) =>
            Event7MembersSummary(fn + " " + ln, paymentValue, paymentValueTwo,  nINumber, memberStatus)
          case (None, Some(ln), _, _, _, _) =>
            Event7MembersSummary(ln, paymentValue, paymentValueTwo,  nINumber, memberStatus)
          case (Some(fn), None, _, _, _, _) =>
            Event7MembersSummary(fn, paymentValue, paymentValueTwo, nINumber, memberStatus)
          case (None, None, _, _, _, _) =>
            Event7MembersSummary(messages("site.notEntered"), paymentValue, paymentValueTwo,  nINumber, memberStatus)
        }
      }
    )

  def readsMember(implicit messages: Messages): Reads[Event7MembersSummary] = {
    JsPath.read(readsMemberSummary)
  }

  private def readsCombinedPaymentValues: Reads[BigDecimal] =
    (readsLumpSumValue and readsCrystallisedValue)(
      (lumpSum, crystallised) => lumpSum + crystallised
    )

  def readsCombinedPayments: Reads[BigDecimal] = {
    JsPath.read(readsCombinedPaymentValues)
  }
}
