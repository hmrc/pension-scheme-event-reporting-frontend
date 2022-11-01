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

package pages.event1

import models.event1.MemberOrEmployerSummary
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import pages.{QuestionPage, Waypoints}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads}
import play.api.mvc.Call

object MembersOrEmployersPage extends QuestionPage[Seq[MemberOrEmployerSummary]] {
  def apply(index: Int): JsPath = path \ index

  def path: JsPath = JsPath \ "event1" \ toString

  override def toString: String = "membersOrEmployers"

  override def route(waypoints: Waypoints): Call = controllers.routes.IndexController.onPageLoad

  private def fail[A]: Reads[A] = Reads.failed[A]("Unknown value")

  val readsMemberOrEmployerValue: Reads[BigDecimal] =
    (JsPath \ "paymentValueAndDate" \ "paymentValue").readNullable[BigDecimal]
      .map(_.getOrElse(BigDecimal(0)))

  private val readsMemberSummary: Reads[MemberOrEmployerSummary] =
    (
      (JsPath \ "membersDetails" \ "firstName").readNullable[String] and
      (JsPath \ "membersDetails" \ "lastName").readNullable[String] and
        readsMemberOrEmployerValue
      ) (
      (firstName, lastName, paymentValue) => {
        (firstName, lastName, paymentValue) match {
          case (Some(fn), Some(ln), _) =>  MemberOrEmployerSummary(fn + " " + ln, paymentValue)
          case (None, Some(ln), _) =>  MemberOrEmployerSummary(ln, paymentValue)
          case (Some(fn), None, _) =>  MemberOrEmployerSummary(fn, paymentValue)
          case (None, None, _) =>  MemberOrEmployerSummary("Not entered", paymentValue)
        }
      }
    )

  private val readsEmployerSummary: Reads[MemberOrEmployerSummary] =
    (
      (JsPath \ "event1" \ "companyDetails" \ "companyName").readNullable[String] and
        readsMemberOrEmployerValue
      ) (
      (companyName, paymentValue) => {
        (companyName, paymentValue) match {
          case (Some(cn), _) =>  MemberOrEmployerSummary(cn, paymentValue)
          case (None, _) =>  MemberOrEmployerSummary("Not entered", paymentValue)
        }
      }
    )

  val readsMemberOrEmployer: Reads[MemberOrEmployerSummary] = {
    (JsPath \ WhoReceivedUnauthPaymentPage.toString).readNullable[String].flatMap{
      case Some(Member.toString) => readsMemberSummary
      case Some(Employer.toString) => readsEmployerSummary
      case None => Reads.pure[MemberOrEmployerSummary](MemberOrEmployerSummary("Not entered", BigDecimal(0.00)))
      case e => fail
    }
  }
}
