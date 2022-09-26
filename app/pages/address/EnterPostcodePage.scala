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
import models.enumeration.AddressJourneyType
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{Page, QuestionPage, Waypoints}

case class EnterPostcodePage(addressJourneyType: AddressJourneyType) extends QuestionPage[String] {

  def getName(userAnswers: UserAnswers): Option[String] = None

  override def path: JsPath = JsPath \ addressJourneyType.eventTypeFragment \ addressJourneyType.addressJourneyTypeFragment \ toString

  override def toString: String = "enterPostcode"

  override def route(waypoints: Waypoints): Call =
    routes.EnterPostcodeController.onPageLoad(waypoints, addressJourneyType)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    ChooseAddressPage(addressJourneyType)
  }
}
