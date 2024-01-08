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

package pages.event10

import controllers.event10.routes
import models.UserAnswers
import models.event10.{BecomeOrCeaseScheme, SchemeChangeDate}
import pages.{JourneyRecoveryPage, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object SchemeChangeDatePage extends QuestionPage[SchemeChangeDate] {

  override def path: JsPath = JsPath \ "event10" \ toString

  override def toString: String = "schemeChangeDate"

  override def route(waypoints: Waypoints): Call =
    routes.SchemeChangeDateController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    val becameRegulatedScheme = BecomeOrCeaseScheme.ItBecameAnInvestmentRegulatedPensionScheme.toString
    answers.get(BecomeOrCeaseSchemePage) match {
      case Some(value) =>
        val becomeOrCeased = value.toString
        if (becomeOrCeased == becameRegulatedScheme) ContractsOrPoliciesPage  else Event10CheckYourAnswersPage()
      case _ => JourneyRecoveryPage
    }
  }
}
