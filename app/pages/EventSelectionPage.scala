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
import pages.common.{ManualOrUploadPage, MembersDetailsPage, MembersOrEmployersPage, MembersPage}
import pages.event10.BecomeOrCeaseSchemePage
import pages.event11.{WhatYouWillNeedPage => event11WhatYouWillNeed}
import pages.event12.HasSchemeChangedRulesPage
import pages.event13.SchemeStructurePage
import pages.event14.HowManySchemeMembersPage
import pages.event18.Event18ConfirmationPage
import pages.event19.{WhatYouWillNeedPage => event19WhatYouWillNeed}
import pages.eventWindUp.SchemeWindUpDatePage
import play.api.libs.json.JsPath
import play.api.mvc.Call
import utils.Event2MemberPageNumbers

case object EventSelectionPage extends QuestionPage[EventSelection] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "EventSelection"

  override def route(waypoints: Waypoints): Call =
    routes.EventSelectionController.onPageLoad(waypoints)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    val optionEventType = answers.get(this).flatMap(es => EventType.fromEventSelection(es))

    optionEventType match {
      case Some(Event1) => ManualOrUploadPage(Event1, answers.countAll(MembersOrEmployersPage(Event1)))
      case Some(Event2) => MembersDetailsPage(Event2, answers.countAll(MembersPage(Event2)), Event2MemberPageNumbers.FIRST_PAGE_DECEASED)
      case Some(Event3) => MembersDetailsPage(Event3, answers.countAll(MembersPage(Event3)))
      case Some(Event4) => MembersDetailsPage(Event4, answers.countAll(MembersPage(Event4)))
      case Some(Event5) => MembersDetailsPage(Event5, answers.countAll(MembersPage(Event5)))
      case Some(Event6) => ManualOrUploadPage(Event6, answers.countAll(MembersPage(Event6)))
      case Some(Event7) => MembersDetailsPage(Event7, answers.countAll(MembersPage(Event7)))
      case Some(Event8) => MembersDetailsPage(Event8, answers.countAll(MembersPage(Event8)))
      case Some(Event8A) => MembersDetailsPage(Event8A, answers.countAll(MembersPage(Event8A)))
      case Some(Event10) => BecomeOrCeaseSchemePage
      case Some(Event11) => event11WhatYouWillNeed
      case Some(Event12) => HasSchemeChangedRulesPage
      case Some(Event13) => SchemeStructurePage
      case Some(Event14) => HowManySchemeMembersPage
      case Some(Event18) => Event18ConfirmationPage
      case Some(Event19) => event19WhatYouWillNeed
      case Some(Event20) => event20.WhatYouWillNeedPage
      case Some(Event22) => ManualOrUploadPage(Event22, answers.countAll(MembersPage(Event22)))
      case Some(Event23) => ManualOrUploadPage(Event23, answers.countAll(MembersPage(Event23)))
      case Some(WindUp) => SchemeWindUpDatePage
      case _ => IndexPage
    }
  }
}
