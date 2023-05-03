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

import data.SampleData
import models.EventSelection.{Event10 => EventSelection10}
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event10, Event22, Event23, Event4, Event5, Event6, Event7, Event8, Event8A}
import models.{EventSelection, UserAnswers}
import pages.behaviours.PageBehaviours
import pages.common.{ManualOrUploadPage, MembersDetailsPage}
import pages.event10.BecomeOrCeaseSchemePage


class EventSelectionPageSpec extends PageBehaviours {

  "nextPageNormalMode" - {
    testGetCorrectPageWithIndexManualOrUpload(Event1, models.EventSelection.Event1, ManualOrUploadPage(Event1, 2))
    testGetCorrectPageWithIndexMemberDetails(Event4, models.EventSelection.Event4, MembersDetailsPage(Event4, 2))
    testGetCorrectPageWithIndexMemberDetails(Event5, models.EventSelection.Event5, MembersDetailsPage(Event5, 2))
    testGetCorrectPageWithIndexManualOrUpload(Event6, models.EventSelection.Event6, ManualOrUploadPage(Event6, 2))
    testGetCorrectPageWithIndexMemberDetails(Event7, models.EventSelection.Event7, MembersDetailsPage(Event7, 2))
    testGetCorrectPageWithIndexMemberDetails(Event8, models.EventSelection.Event8, MembersDetailsPage(Event8, 2))
    testGetCorrectPageWithIndexMemberDetails(Event8A, models.EventSelection.Event8A, MembersDetailsPage(Event8A, 2))
    testGetCorrectPageWithIndexManualOrUpload(Event22, models.EventSelection.Event22, ManualOrUploadPage(Event22, 2))
    testGetCorrectPageWithIndexManualOrUpload(Event23, models.EventSelection.Event23, ManualOrUploadPage(Event23, 2))
    testGetCorrectPageEvent10
  }

  private def testGetCorrectPageEvent10: Unit = {
    s"must get the correct page for Event 10" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, EventSelection10)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe BecomeOrCeaseSchemePage.route(EmptyWaypoints)
    }
  }

  private def testGetCorrectPageWithIndexManualOrUpload(eventType: EventType, eventSelection: EventSelection, page: ManualOrUploadPage): Unit = {
    s"must get the correct page with correct index for Event $eventType" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, eventSelection)
        .setOrException(MembersDetailsPage(eventType, 0), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(eventType, 1), SampleData.memberDetails)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe page.route(EmptyWaypoints)
    }
  }

  private def testGetCorrectPageWithIndexMemberDetails(eventType: EventType, eventSelection: EventSelection, page: MembersDetailsPage): Unit = {
    s"must get the correct page with correct index for Event $eventType" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, eventSelection)
        .setOrException(MembersDetailsPage(eventType, 0), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(eventType, 1), SampleData.memberDetails)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe page.route(EmptyWaypoints)
    }
  }
}
