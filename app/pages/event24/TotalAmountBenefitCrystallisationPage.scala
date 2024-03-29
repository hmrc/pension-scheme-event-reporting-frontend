/*
 * Copyright 2024 HM Revenue & Customs
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

package pages.event24

import controllers.event24.routes
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.common.MembersPage
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{Page, QuestionPage, Waypoints}

case class TotalAmountBenefitCrystallisationPage(index: Index) extends QuestionPage[BigDecimal] {
  override def path: JsPath = MembersPage(EventType.Event24)(index) \ TotalAmountBenefitCrystallisationPage.toString
  override def route(waypoints: Waypoints): Call =
    routes.TotalAmountBenefitCrystallisationController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    ValidProtectionPage(index)
}

object TotalAmountBenefitCrystallisationPage {
  override def toString: String = "totalAmountBenefitCrystallisation"
}
