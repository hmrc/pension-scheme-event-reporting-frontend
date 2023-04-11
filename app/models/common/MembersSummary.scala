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

package models.common

import models.enumeration.EventType
import models.enumeration.EventType._
import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Format, JsPath, Json, Reads}

case class MembersSummary(name: String, PaymentValue: BigDecimal, nINumber: String)

object MembersSummary {
  implicit lazy val formats: Format[MembersSummary] = Json.format[MembersSummary]

  private def readsNINumber(implicit messages: Messages): Reads[String] =
    (JsPath \ "membersDetails" \ "nino").readNullable[String]
      .map(_.getOrElse(messages("site.notEntered")))

  def readsMemberValue(eventType: EventType): Reads[BigDecimal] =
    memberValuePath(eventType).readNullable[BigDecimal]
      .map(_.getOrElse(BigDecimal(0)))

  private def readsMemberSummary(eventType: EventType)(implicit messages: Messages): Reads[MembersSummary] =
    (
      (JsPath \ "membersDetails" \ "firstName").readNullable[String] and
        (JsPath \ "membersDetails" \ "lastName").readNullable[String] and
        readsMemberValue(eventType) and
        readsNINumber
      )(
      (firstName, lastName, paymentValue, nINumber) => {
        (firstName, lastName, paymentValue, nINumber) match {
          case (Some(fn), Some(ln), _, _) => MembersSummary(fn + " " + ln, paymentValue, nINumber)
          case (None, Some(ln), _, _) => MembersSummary(ln, paymentValue, nINumber)
          case (Some(fn), None, _, _) => MembersSummary(fn, paymentValue, nINumber)
          case (None, None, _, _) => MembersSummary(messages("site.notEntered"), paymentValue, nINumber)
        }
      }
    )

  def readsMember(eventType: EventType)(implicit messages: Messages): Reads[MembersSummary] = {
    JsPath.read(readsMemberSummary(eventType))
  }

  def memberValuePath(eventType: EventType): JsPath = eventType match {
    case Event3 | Event4 | Event5 => JsPath \ "paymentDetails" \ "amountPaid"
    case Event6 => JsPath \ "AmountCrystallisedAndDate" \ "amountCrystallised"
    case Event8 => JsPath \ "lumpSumAmountAndDate" \ "lumpSumAmount"
    case Event8A => JsPath \ "lumpSumAmountAndDate" \ "lumpSumAmount"
    case _ => JsPath \ "totalPensionAmounts"
  }

}
