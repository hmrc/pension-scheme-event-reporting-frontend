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

package pages.event3

import controllers.event3.routes
import models.UserAnswers
import models.enumeration.EventType
import models.enumeration.EventType.Event3
import models.event3.ReasonForBenefits
import models.event3.ReasonForBenefits.Other
import pages.common.{MembersPage, PaymentDetailsPage}
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class ReasonForBenefitsPage(index: Int) extends QuestionPage[ReasonForBenefits] {

  override def path: JsPath = MembersPage(EventType.Event3)(index) \ "benefitType" \ toString

  override def toString: String = "reasonBenefitTaken"

  override def route(waypoints: Waypoints): Call =
    routes.ReasonForBenefitsController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this) match {
      case Some(Other) => EarlyBenefitsBriefDescriptionPage(index)
      case _ => PaymentDetailsPage(Event3, index)
    }
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page =
    updatedAnswers.get(this) match {
      case Some(Other) => EarlyBenefitsBriefDescriptionPage(index)
      case _ => PaymentDetailsPage(Event3, index)
    }



}
