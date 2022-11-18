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

package pages.event23

import controllers.event23.routes
import models.{Index, UserAnswers}
import models.event23.HowAddDualAllowance
import models.event23.HowAddDualAllowance.Manual
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class HowAddDualAllowancePage(index: Index) extends QuestionPage[HowAddDualAllowance] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "howAddDualAllowance"

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this) match {
      case Some(Manual) => WhatYouWillNeedPage(index)
      case _ => this
    }

  override def route(waypoints: Waypoints): Call = {
    routes.HowAddDualAllowanceController.onPageLoad(waypoints, index)
  }
}
