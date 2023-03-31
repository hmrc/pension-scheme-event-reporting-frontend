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
import models.UserAnswers
import models.enumeration.EventType.{Event1, Event22, Event23, Event6, Event8, Event8A}
import pages.behaviours.PageBehaviours
import pages.common.{ManualOrUploadPage, MembersDetailsPage}


class EventSelectionPageSpec extends PageBehaviours {

  "nextPageNormalMode" - {
    "must get the correct page with correct index for event 1" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, models.EventSelection.Event1)
        .setOrException(MembersDetailsPage(Event1, 0), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(Event1, 1), SampleData.memberDetails)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe ManualOrUploadPage(Event1, 2).route(EmptyWaypoints)
    }
    "must get the correct page with correct index for event 6" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, models.EventSelection.Event6)
        .setOrException(MembersDetailsPage(Event6, 0), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(Event6, 1), SampleData.memberDetails)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe ManualOrUploadPage(Event6, 2).route(EmptyWaypoints)
    }
    "must get the correct page with correct index for event 8" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, models.EventSelection.Event8)
        .setOrException(MembersDetailsPage(Event8, 0), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(Event8, 1), SampleData.memberDetails)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe MembersDetailsPage(Event8, 2).route(EmptyWaypoints)
    }
    "must get the correct page with correct index for event 8A" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, models.EventSelection.Event8A)
        .setOrException(MembersDetailsPage(Event8A, 0), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(Event8A, 1), SampleData.memberDetails)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe MembersDetailsPage(Event8A, 2).route(EmptyWaypoints)
    }
    "must get the correct page with correct index for event 22" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, models.EventSelection.Event22)
        .setOrException(MembersDetailsPage(Event22, 0), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(Event22, 1), SampleData.memberDetails)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe ManualOrUploadPage(Event22, 2).route(EmptyWaypoints)
    }
    "must get the correct page with correct index for event 23" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, models.EventSelection.Event23)
        .setOrException(MembersDetailsPage(Event23, 0), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(Event23, 1), SampleData.memberDetails)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe ManualOrUploadPage(Event23, 2).route(EmptyWaypoints)
    }

  }
}
