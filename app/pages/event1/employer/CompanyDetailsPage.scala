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

package pages.event1.employer

import controllers.event1.employer.routes
import models.UserAnswers
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import models.enumeration.EventType.Event1
import models.event1.employer.CompanyDetails
import pages.event1.MembersOrEmployersPage
import pages.{CheckYourAnswersPage, NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class CompanyDetailsPage(index: Int) extends QuestionPage[CompanyDetails] {

  override def path: JsPath = MembersOrEmployersPage(index) \ "event1" \ toString

  override def toString: String = "companyDetails"

  override def route(waypoints: Waypoints): Call =
    routes.CompanyDetailsController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    pages.address.EnterPostcodePage(Event1EmployerAddressJourney, index)

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page =
    CheckYourAnswersPage(Event1, Some(index))


}
