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

package pages.event13

import controllers.event13.routes
import models.UserAnswers
import models.event13.SchemeStructure
import models.event13.SchemeStructure
import pages.{IndexPage, NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object SchemeStructurePage extends QuestionPage[SchemeStructure] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "schemeStructure"

  override def route(waypoints: Waypoints): Call =
    routes.SchemeStructureController.onPageLoad(waypoints)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(SchemeStructurePage) match {
      case Some(SchemeStructure.Single) => ChangeDatePage
      case Some(SchemeStructure.Group) => ChangeDatePage
      case Some(SchemeStructure.Corporate) => ChangeDatePage
      case Some(SchemeStructure.Other) => SchemeStructureDescriptionPage
      case _ => IndexPage
    }
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page = {
    answers.get(SchemeStructurePage) match {
      case Some(SchemeStructure.Other) => SchemeStructureDescriptionPage
      case _ => super.nextPageCheckMode(waypoints, answers)
    }
  }
}
