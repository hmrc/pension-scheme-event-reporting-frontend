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

package pages.event1

import controllers.event1.routes
import models.UserAnswers
import models.enumeration.EventType
import pages.common.MembersOrEmployersPage
import pages.event1.member.PaymentNaturePage
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class ValueOfUnauthorisedPaymentPage(index: Int) extends QuestionPage[Boolean] {

  override def path: JsPath = MembersOrEmployersPage(EventType.Event1)(index) \ toString

  override def toString: String = "valueOfUnauthorisedPayment"

  override def route(waypoints: Waypoints): Call =
    routes.ValueOfUnauthorisedPaymentController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this).map {
      case true => SchemeUnAuthPaySurchargeMemberPage(index)
      case false => PaymentNaturePage(index)
    }.orRecover
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    updatedAnswers.get(this).map {
      case true => SchemeUnAuthPaySurchargeMemberPage(index)
      case false => Event1CheckYourAnswersPage(index)
    }.orRecover
  }
}
