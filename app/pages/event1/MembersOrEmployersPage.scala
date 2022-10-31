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
import play.api.libs.json.{JsPath, Reads}
import play.api.mvc.Call
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case object MembersOrEmployersPage extends QuestionPage[Seq[MemberOrEmployerSummary]] {
  def apply(index: Int): JsPath = path \ index

  def path: JsPath = JsPath \ "event1" \ toString

  override def toString: String = "membersOrEmployers"

  override def route(waypoints: Waypoints): Call = controllers.routes.IndexController.onPageLoad

  /*
  * "event1" : {
            "membersOrEmployers" : [
                {
                    "howAddUnauthPayment" : "manual",
                    "whoReceivedUnauthPayment" : "member",
                    "membersDetails" : {
                        "firstName" : "a",
                        "lastName" : "a",
                        "nino" : "CS121212C"
                    },
                    "doYouHoldSignedMandate" : true,
                    "valueOfUnauthorisedPayment" : false,
                    "paymentNatureMember" : "memberOther",
                    "memberPaymentNatureDescription" : "dsds",
                    "paymentValueAndDate" : {
                        "paymentValue" : 12.12,
                        "paymentDate" : "2022-05-01"
                    }
                }
            ]
        }
        *
        *
        "event1": {
              "companyDetails": {
                "companyName": "Test Co.",
                "companyNumber": "01234567"
              },
              "employerAddress": {
                "address": {
                  "addressLine1": "10 Other Place",
                  "addressLine2": "Some District",
                  "addressLine3": "Anytown",
                  "postcode": "ZZ1 1ZZ",
                  "country": "GB"
                }
              }
            }*
        *
  * */


  private def fail[A]: Reads[A] = Reads.failed[A]("Unknown value")

  private val readsMemberSummary: Reads[MemberOrEmployerSummary] =
    (
      (JsPath \ "membersDetails" \ "firstName").read[String] and
      (JsPath \ "membersDetails" \ "lastName").read[String] and
      (JsPath \ "paymentValueAndDate" \ "paymentValue").read[BigDecimal]
      ) (
      (firstName, lastName, paymentValue) => MemberOrEmployerSummary(firstName + " " + lastName, paymentValue, 0)
    )

  private val readsEmployerSummary: Reads[MemberOrEmployerSummary] =
    (
      (JsPath \ "event1" \ "companyDetails" \ "companyName").read[String] and
        (JsPath \ "paymentValueAndDate" \ "paymentValue").read[BigDecimal]
      ) (
      (companyName, paymentValue) => MemberOrEmployerSummary(companyName, paymentValue, 0)
    )

  private val readsMemberOrEmployer: Reads[MemberOrEmployerSummary] = {
    (JsPath \ WhoReceivedUnauthPaymentPage.toString).read[String].flatMap{
      case Member.toString => readsMemberSummary
      case Employer.toString => readsEmployerSummary
      case e => fail
    }
  }


  implicit val readsMemberOrEmployerSummary: Reads[Seq[MemberOrEmployerSummary]] = {
    path.read[Seq[MemberOrEmployerSummary]](Reads.seq(readsMemberOrEmployer)).map{ xx =>
      xx.zipWithIndex.map{ case (d, i) =>
        d copy (index = i)
      }
    }
  }

}
