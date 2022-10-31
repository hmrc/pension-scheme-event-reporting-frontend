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
import pages.common.MembersDetailsPage
import pages.{QuestionPage, Waypoints}
import play.api.libs.json.{JsPath, Reads}
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved

case object MembersOrEmployersPage extends QuestionPage[Seq[MemberOrEmployerSummary]] {
  def apply(index: Int) : JsPath = path \ index
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
  * */
  val readsMemberOrEmployerSummary: Reads[MemberOrEmployerSummary] =
    ((JsPath \ MembersDetailsPage.toString \ "firstName") and
      (JsPath \ MembersDetailsPage.toString \ "lastName") and
      (JsPath \ PaymentValueAndDatePage.toString \ "paymentValue"))(a, b)

  implicit val reads: Reads[Seq[MemberOrEmployerSummary]] = {
      path.read[Seq[MemberOrEmployerSummary]](Reads.seq(readsMemberOrEmployerSummary))
  }
  //    JsPath.read[String].flatMap {
//      case aop if mappings.keySet.contains(aop) => Reads(_ => JsSuccess(mappings.apply(aop)))
//      case invalidValue => Reads(_ => JsError(s"Invalid administrator or practitioner type: $invalidValue"))
//    }

}




