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

package pages

import controllers.routes
import models.enumeration.EventType
import models.enumeration.EventType._
import models.{EventSelection, UserAnswers}
import pages.common.{ManualOrUploadPage, MembersOrEmployersPage, MembersPage}
import pages.event18.Event18ConfirmationPage
import pages.eventWindUp.SchemeWindUpDatePage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object EventSelectionPage extends QuestionPage[EventSelection] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "EventSelection"

  override def route(waypoints: Waypoints): Call =
    routes.EventSelectionController.onPageLoad(waypoints)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    val optionEventType = answers.get(this).flatMap(es => EventType.fromEventSelection(es))

    optionEventType match {
      case Some(Event1) => ManualOrUploadPage(Event1, answers.countAll(MembersOrEmployersPage(Event1)))
      case Some(Event18) => Event18ConfirmationPage
      case Some(Event22) => ManualOrUploadPage(Event22, answers.countAll(MembersPage(Event22)))
      case Some(Event23) => ManualOrUploadPage(Event23, answers.countAll(MembersPage(Event23)))
      case Some(WindUp) => SchemeWindUpDatePage
      case _ => IndexPage
    }
  }
}
