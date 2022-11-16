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

package pages.common

import models.UserAnswers
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
import pages.event23.Event23CheckYourAnswersPage
import pages.{IndexPage, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class TotalPensionAmountsPage(eventType: EventType) extends QuestionPage[BigDecimal] {

  override def path: JsPath = JsPath \ s"event${eventType.toString}" \ toString

  override def toString: String = "totalPensionAmounts"

  override def route(waypoints: Waypoints): Call =
    controllers.common.routes.TotalPensionAmountsController.onPageLoad(waypoints, eventType)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    eventType  match {
      case Event22 => IndexPage
      case Event23 => Event23CheckYourAnswersPage
      case _ => IndexPage
    }

}
