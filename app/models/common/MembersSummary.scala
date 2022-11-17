/*
 * Copyright 2022 HM Revenue & Customs
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

import pages.Page
import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Format, JsPath, Json, Reads}

case class MembersSummary(name: String, PaymentValue: BigDecimal, nINumber: String)

object MembersSummary {
  implicit lazy val formats: Format[MembersSummary] = Json.format[MembersSummary]

  private def fail[A]: Reads[A] = Reads.failed[A]("Unknown value")

  private def readsNINumber(implicit messages: Messages): Reads[String] =
    (JsPath \ "membersDetails" \ "nino").readNullable[String]
      .map(_.getOrElse(messages("site.notEntered")))

  val readsMemberValue: Reads[BigDecimal] =
    (JsPath \ "paymentValueAndDate" \ "paymentValue").readNullable[BigDecimal]
      .map(_.getOrElse(BigDecimal(0)))

  private def readsMemberSummary(implicit messages: Messages): Reads[MembersSummary] =
    (
      (JsPath \ "membersDetails" \ "firstName").readNullable[String] and
        (JsPath \ "membersDetails" \ "lastName").readNullable[String] and
        readsMemberValue and
        readsNINumber
      ) (
      (firstName, lastName, paymentValue, nINumber) => {
        (firstName, lastName, paymentValue, nINumber) match {
          case (Some(fn), Some(ln), _, _) =>  MembersSummary(fn + " " + ln, paymentValue, nINumber)
          case (None, Some(ln), _, _) =>  MembersSummary(ln, paymentValue, nINumber)
          case (Some(fn), None, _, _) =>  MembersSummary(fn, paymentValue, nINumber)
          case (None, None, _, _) =>  MembersSummary(messages("site.notEntered"), paymentValue, nINumber)
        }
      }
    )


  def readsMember(page: Page)(implicit messages: Messages): Reads[MembersSummary] = {
    (JsPath \ page.toString).readNullable[String].flatMap{
      case Some(_) => readsMemberSummary
      case None => Reads.pure[MembersSummary](MembersSummary(messages("site.notEntered"), BigDecimal(0.00), messages("site.notEntered")))
      case e => fail
    }
  }
}
