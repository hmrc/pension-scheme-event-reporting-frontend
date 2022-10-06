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

import controllers.event1.routes
import models.UserAnswers
import models.enumeration.AddressJourneyType.Event1MemberPropertyAddressJourney
import models.event1.PaymentNature
import models.event1.PaymentNature.{BenefitInKind, BenefitsPaidEarly, ErrorCalcTaxFreeLumpSums, OverpaymentOrWriteOff, RefundOfContributions, ResidentialPropertyHeld}
import pages.{IndexPage, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call


case object PaymentNaturePage extends QuestionPage[PaymentNature] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "paymentNature"

  def route(waypoints: Waypoints): Call =
    routes.PaymentNatureController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(PaymentNaturePage) match {
      case Some(BenefitInKind) => BenefitInKindBriefDescriptionPage
      case Some(ErrorCalcTaxFreeLumpSums) => pages.event1.member.ErrorDescriptionPage
      case Some(ResidentialPropertyHeld) => pages.address.EnterPostcodePage(Event1MemberPropertyAddressJourney)
      case Some(BenefitsPaidEarly) => pages.event1.member.BenefitsPaidEarlyPage
      case Some(OverpaymentOrWriteOff) => pages.event1.member.ReasonForTheOverpaymentOrWriteOffPage
      case Some(RefundOfContributions) => pages.event1.member.RefundOfContributionsPage
      case _ => IndexPage
    }
  }
}
