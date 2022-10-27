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
import models.enumeration.AddressJourneyType.Event1EmployerPropertyAddressJourney
import models.event1.employer.PaymentNature
import models.event1.employer.PaymentNature.{CourtOrder, LoansExceeding50PercentOfFundValue, Other, ResidentialProperty, TangibleMoveableProperty}
import pages.{IndexPage, MembersOrEmployersPage, NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class PaymentNaturePage(index: Int) extends QuestionPage[PaymentNature] {

  override def path: JsPath = MembersOrEmployersPage(index).path \ toString

  override def toString: String = "paymentNature"

  override def route(waypoints: Waypoints): Call =
    routes.PaymentNatureController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(PaymentNaturePage(index)) match {
      case Some(LoansExceeding50PercentOfFundValue) => LoanDetailsPage(index)
      case Some(ResidentialProperty) => pages.address.EnterPostcodePage(Event1EmployerPropertyAddressJourney, index)
      case Some(TangibleMoveableProperty) => EmployerTangibleMoveablePropertyPage(index)
      case Some(CourtOrder) => UnauthorisedPaymentRecipientNamePage(index)
      case Some(Other) => EmployerPaymentNatureDescriptionPage(index)
      case _ => IndexPage
    }
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    updatedAnswers.get(PaymentNaturePage(index)) match {
      case Some(LoansExceeding50PercentOfFundValue) => LoanDetailsPage(index)
      case Some(ResidentialProperty) => pages.address.EnterPostcodePage(Event1EmployerPropertyAddressJourney, index)
      case Some(TangibleMoveableProperty) => EmployerTangibleMoveablePropertyPage(index)
      case Some(CourtOrder) => UnauthorisedPaymentRecipientNamePage(index)
      case Some(Other) => EmployerPaymentNatureDescriptionPage(index)
      case _ => IndexPage
    }
  }
}
