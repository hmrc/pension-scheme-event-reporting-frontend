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

package pages.address

import controllers.address.routes
import models.UserAnswers
import models.address.Address
import models.enumeration.{AddressJourneyType, EventType}
import models.enumeration.AddressJourneyType.{Event1EmployerAddressJourney, Event1EmployerPropertyAddressJourney, Event1MemberPropertyAddressJourney}
import pages.event1.PaymentValueAndDatePage
import pages.event1.employer.PaymentNaturePage
import pages.{MembersOrEmployersPage, Page, QuestionPage, Waypoints}
import pages.{CheckYourAnswersPage, NonEmptyWaypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class ManualAddressPage(addressJourneyType: AddressJourneyType, index: Int) extends QuestionPage[Address] {

  override def path: JsPath = MembersOrEmployersPage(index).path \ s"event${addressJourneyType.eventType.toString}" \ addressJourneyType.nodeName \ toString

  override def toString: String = "address"

  override def route(waypoints: Waypoints): Call =
    routes.ManualAddressController.onPageLoad(waypoints, addressJourneyType, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    addressJourneyType match {
      case Event1EmployerAddressJourney => PaymentNaturePage(index)
      case Event1MemberPropertyAddressJourney | Event1EmployerPropertyAddressJourney => PaymentValueAndDatePage(index)
      case _ => super.nextPageNormalMode(waypoints, answers)
    }
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    addressJourneyType match {
      case Event1EmployerAddressJourney => CheckYourAnswersPage(EventType.Event1)
      case Event1MemberPropertyAddressJourney  | Event1EmployerPropertyAddressJourney => PaymentValueAndDatePage(index)
      case _ => super.nextPageNormalMode(waypoints, updatedAnswers)
    }
  }

}
