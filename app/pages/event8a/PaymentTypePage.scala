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

package pages.event8a

import controllers.event8a.routes
import models.UserAnswers
import models.enumeration.EventType
import models.event8a.PaymentType
import pages.common.MembersPage
import pages.{IndexPage, JourneyRecoveryPage, NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.{Success, Try}

case class PaymentTypePage(eventType: EventType, index: Int) extends QuestionPage[PaymentType] {

  override def path: JsPath = MembersPage(EventType.Event8A)(index) \ toString

  override def toString: String = "paymentType"

  override def route(waypoints: Waypoints): Call =
    routes.PaymentTypeController.onPageLoad(waypoints, index)

  override def cleanupBeforeSettingValue(value: Option[PaymentType], userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.get(PaymentTypePage(eventType, index)) match {
      case originalPaymentType@Some(_) if originalPaymentType != value =>
        userAnswers.remove(TypeOfProtectionPage(eventType, index)).
          flatMap(ua => ua.remove(TypeOfProtectionReferencePage(eventType, index)))
      case _ => Success(userAnswers)
    }
  }
  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {

    val optionSelected = answers.get(PaymentTypePage(eventType, index))
    optionSelected match {
      case Some(paymentType) =>
        paymentType match {
          case PaymentType.PaymentOfAStandAloneLumpSum =>
            TypeOfProtectionPage(eventType, index)
          case PaymentType.PaymentOfASchemeSpecificLumpSum =>
            LumpSumAmountAndDatePage(eventType, index)
        }
      case _ =>
        JourneyRecoveryPage
    }
  }
  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {

    val originalOptionSelected = originalAnswers.get(PaymentTypePage(eventType, index))
    val updatedOptionSelected = updatedAnswers.get(PaymentTypePage(eventType, index))
    val answerIsChanged = originalOptionSelected != updatedOptionSelected

    answerIsChanged match {
      case true =>
        updatedOptionSelected match {
          case Some(paymentType) =>
            paymentType match {
              case PaymentType.PaymentOfAStandAloneLumpSum =>
                TypeOfProtectionPage(eventType, index)
              case PaymentType.PaymentOfASchemeSpecificLumpSum =>
                Event8ACheckYourAnswersPage(index)
            }
          case _ =>
            JourneyRecoveryPage
        }
      case false =>
        Event8ACheckYourAnswersPage(index)
    }
  }
}