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

package pages.common

import models.Index.{indexPathBindable, intToIndex}
import models.UserAnswers
import models.enumeration.EventType
import models.enumeration.EventType._
import pages.event1.Event1CheckYourAnswersPage
import pages.event2.Event2CheckYourAnswersPage
import pages.event22.Event22CheckYourAnswersPage
import pages.event23.Event23CheckYourAnswersPage
import pages.event3.Event3CheckYourAnswersPage
import pages.event4.Event4CheckYourAnswersPage
import pages.event5.Event5CheckYourAnswersPage
import pages.event6.Event6CheckYourAnswersPage
import pages.event7.Event7CheckYourAnswersPage
import pages.event8.Event8CheckYourAnswersPage
import pages.event8a.Event8ACheckYourAnswersPage
import pages.{EventSelectionPage, NonEmptyWaypoints, Page, Waypoints}
import play.api.mvc.Call

// TODO: remove scalastyle off comment
//scalastyle:off
case class MembersDetailsBackLinkPage(eventType: EventType, index: Int, memberPageNo: Int = 0) extends Page {

  override def route(waypoints: Waypoints): Call =
    controllers.common.routes.MembersDetailsBackLinkController.onBackClick(waypoints, eventType, index, memberPageNo)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    (eventType, index, memberPageNo) match {
      case (Event1, _, _) => pages.event1.WhatYouWillNeedPage(index)
      case (Event2, _, 2) => pages.common.MembersDetailsPage(Event2, index, 1)
      // TODO: add Event24 to this case when merged in
      case (Event6, _, _) | (Event22, _, _)
           | (Event23, _, _) => pages.common.ManualOrUploadPage(eventType, index)
      case (Event2, _, 1) | (Event3, _, _)
           | (Event4, _, _) |(Event5, _, _)
           | (Event7, _, _) | (Event8, _, _)
           | (Event8A, _, _) => EventSelectionPage
      case _ => super.nextPageNormalMode(waypoints, answers)
    }
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page =
    (eventType, index, memberPageNo) match {
      case (Event1, index, _) => Event1CheckYourAnswersPage(index)
      case (Event2, index, _) => Event2CheckYourAnswersPage(index)
      case (Event3, index, _) => Event3CheckYourAnswersPage(index)
      case (Event4, index, _) => Event4CheckYourAnswersPage(index)
      case (Event5, index, _) => Event5CheckYourAnswersPage(index)
      case (Event6, index, _) => Event6CheckYourAnswersPage(index)
      case (Event7, index, _) => Event7CheckYourAnswersPage(index)
      case (Event8, index, _) => Event8CheckYourAnswersPage(index)
      case (Event8A, index, _) => Event8ACheckYourAnswersPage(index)
      case (Event22, index, _) => Event22CheckYourAnswersPage(index)
      case (Event23, index, _) => Event23CheckYourAnswersPage(index)
      // TODO: add Event24 when merged in
      case _ => super.nextPageNormalMode(waypoints, updatedAnswers)
    }
}
