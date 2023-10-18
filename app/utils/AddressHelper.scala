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

package utils

import models.Index
import models.enumeration.EventType.Event1
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.requests.DataRequest
import pages.common.MembersDetailsPage
import pages.event1.WhoReceivedUnauthPaymentPage
import pages.event1.employer.CompanyDetailsPage
import play.api.mvc.AnyContent

object AddressHelper {

  private val defaultNames = ("the company", "the member", "the entry")

  val retrieveNameManual: (DataRequest[AnyContent], Index) => String = (dataRequest: DataRequest[AnyContent], index: Index) => {
    dataRequest.userAnswers.get(WhoReceivedUnauthPaymentPage(index)) match {
      case Some(Employer) => dataRequest.userAnswers.get(CompanyDetailsPage(index)).map(_.companyName).getOrElse(defaultNames._1)
      case Some(Member) => dataRequest.userAnswers.get(MembersDetailsPage(Event1, index)).map(_.fullName).getOrElse(defaultNames._2)
      case _ => defaultNames._3
    }
  }
}
