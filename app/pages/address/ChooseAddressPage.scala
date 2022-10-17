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
import models.enumeration.AddressJourneyType
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import pages.event1.employer.PaymentNaturePage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class ChooseAddressPage(addressJourneyType: AddressJourneyType) extends QuestionPage[Address] {

  override def path: JsPath = JsPath \ s"event${addressJourneyType.eventType.toString}" \ addressJourneyType.nodeName \ toString

  override def toString: String = "chooseAddress"

  override def route(waypoints: Waypoints): Call =
    routes.ChooseAddressController.onPageLoad(waypoints, addressJourneyType)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    addressJourneyType match {
      case Event1EmployerAddressJourney => PaymentNaturePage

      case _ => super.nextPageNormalMode(waypoints, answers)
    }
  }

}
