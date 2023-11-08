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
import models.enumeration.EventType.{Event1, Event2, Event22, Event23, Event3, Event4, Event5, Event6, Event7, Event8, Event8A}
import models.{EventSelection, UserAnswers}
import pages.behaviours.PageBehaviours
import pages.common.{ManualOrUploadPage, MembersDetailsPage, PaymentDetailsPage, TotalPensionAmountsPage}
import pages.event1.PaymentValueAndDatePage
import pages.event10.BecomeOrCeaseSchemePage
import pages.event2.DatePaidPage
import pages.event6.AmountCrystallisedAndDatePage
import pages.event7.PaymentDatePage
import pages.event8.LumpSumAmountAndDatePage


class EventSelectionPageSpec extends PageBehaviours {

  "nextPageNormalMode" - {
    testGetCorrectPageEvent1()
    testGetCorrectPageEvent2()
    testGetCorrectPageEvent3or4or5(Event3, models.EventSelection.Event3)
    testGetCorrectPageEvent3or4or5(Event4, models.EventSelection.Event4)
    testGetCorrectPageEvent3or4or5(Event5, models.EventSelection.Event5)
    testGetCorrectPageEvent6()
    testGetCorrectPageEvent7()
    testGetCorrectPageEvent8or8a(Event8, models.EventSelection.Event8)
    testGetCorrectPageEvent8or8a(Event8A, models.EventSelection.Event8A)
    testGetCorrectPageEvent22or23(Event22, models.EventSelection.Event22)
    testGetCorrectPageEvent22or23(Event23, models.EventSelection.Event23)
    testRedirectToIncompleteIndexManualOrUpload(Event1,  models.EventSelection.Event1,  ManualOrUploadPage(Event1, 2))
    testRedirectToIncompleteIndexManualOrUpload(Event6,  models.EventSelection.Event6,  ManualOrUploadPage(Event6, 2))
    testRedirectToIncompleteIndexManualOrUpload(Event22, models.EventSelection.Event22, ManualOrUploadPage(Event22, 2))
    testRedirectToIncompleteIndexManualOrUpload(Event23, models.EventSelection.Event23, ManualOrUploadPage(Event23, 2))
    testRedirectToIncompleteIndexMemberDetails(Event2,  models.EventSelection.Event2,  MembersDetailsPage(Event2, 2, 1))
    testRedirectToIncompleteIndexMemberDetails(Event3,  models.EventSelection.Event3,  MembersDetailsPage(Event3, 2))
    testRedirectToIncompleteIndexMemberDetails(Event4,  models.EventSelection.Event4,  MembersDetailsPage(Event4, 2))
    testRedirectToIncompleteIndexMemberDetails(Event5,  models.EventSelection.Event5,  MembersDetailsPage(Event5, 2))
    testRedirectToIncompleteIndexMemberDetails(Event7,  models.EventSelection.Event7,  MembersDetailsPage(Event7, 2))
    testRedirectToIncompleteIndexMemberDetails(Event8,  models.EventSelection.Event8,  MembersDetailsPage(Event8, 2))
    testRedirectToIncompleteIndexMemberDetails(Event8A, models.EventSelection.Event8A, MembersDetailsPage(Event8A, 2))
    testGetCorrectPageEvent10()
  }

  private def testGetCorrectPageEvent1(): Unit = {
    s"must get the correct page with correct index for Event 1" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, models.EventSelection.Event1)
        .setOrException(MembersDetailsPage(Event1, 0), SampleData.memberDetails)
        .setOrException(PaymentValueAndDatePage(0), SampleData.paymentDetails)

      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe
        ManualOrUploadPage(Event1, 1).route(EmptyWaypoints)
    }
  }

  private def testGetCorrectPageEvent2(): Unit = {
    s"must get the correct page with correct index for Event 2" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, models.EventSelection.Event2)
        .setOrException(MembersDetailsPage(Event2, 0, 1), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(Event2, 0, 2), SampleData.memberDetails2)
        .setOrException(DatePaidPage(0, Event2), SampleData.datePaid)

      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe
        MembersDetailsPage(Event2, 1, 1).route(EmptyWaypoints)
    }
  }

  private def testGetCorrectPageEvent3or4or5(eventType: EventType, eventSelection: EventSelection): Unit = {
    s"must get the correct page with correct index for Event $eventType" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, eventSelection)
        .setOrException(MembersDetailsPage(eventType, 0), SampleData.memberDetails)
        .setOrException(PaymentDetailsPage(eventType, 0), SampleData.paymentDetailsCommon)

      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe
        MembersDetailsPage(eventType, 1).route(EmptyWaypoints)
    }
  }

  private def testGetCorrectPageEvent6(): Unit = {
    s"must get the correct page with correct index for Event 6" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, models.EventSelection.Event6)
        .setOrException(MembersDetailsPage(Event6, 0), SampleData.memberDetails)
        .setOrException(AmountCrystallisedAndDatePage(Event6, 0), SampleData.crystallisedDetails)

      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe
        ManualOrUploadPage(Event6, 1).route(EmptyWaypoints)
    }
  }

  private def testGetCorrectPageEvent7(): Unit = {
    s"must get the correct page with correct index for Event 7" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, models.EventSelection.Event7)
        .setOrException(MembersDetailsPage(Event7, 0), SampleData.memberDetails)
        .setOrException(PaymentDatePage(0), SampleData.event7PaymentDate)

      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe
        MembersDetailsPage(Event7, 1).route(EmptyWaypoints)
    }
  }

  private def testGetCorrectPageEvent8or8a(eventType: EventType, eventSelection: EventSelection): Unit = {
    s"must get the correct page with correct index for Event $eventType" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, eventSelection)
        .setOrException(MembersDetailsPage(eventType, 0), SampleData.memberDetails)
        .setOrException(LumpSumAmountAndDatePage(eventType, 0), SampleData.lumpSumDetails)

      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe
        MembersDetailsPage(eventType, 1).route(EmptyWaypoints)
    }
  }

  private def testGetCorrectPageEvent22or23(eventType: EventType, eventSelection: EventSelection): Unit = {
    s"must get the correct page with correct index for Event $eventType" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, eventSelection)
        .setOrException(MembersDetailsPage(eventType, 0), SampleData.memberDetails)
        .setOrException(TotalPensionAmountsPage(eventType, 0), SampleData.totalPaymentAmountEvent22and23)

      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe
        ManualOrUploadPage(eventType, 1).route(EmptyWaypoints)
    }
  }

  private def testRedirectToIncompleteIndexManualOrUpload(eventType: EventType, eventSelection: EventSelection, page: ManualOrUploadPage): Unit = {
    s"must redirect to complete missing journey data for first entry in Event $eventType" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, eventSelection)
        .setOrException(MembersDetailsPage(eventType, 0), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(eventType, 1), SampleData.memberDetails2)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe
        ManualOrUploadPage(eventType, index = 0).route(EmptyWaypoints)
    }
  }

  private def testRedirectToIncompleteIndexMemberDetails(eventType: EventType, eventSelection: EventSelection, page: MembersDetailsPage): Unit = {
    s"must redirect to complete missing journey data for first entry in Event $eventType" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, eventSelection)
        .setOrException(MembersDetailsPage(eventType, 0), SampleData.memberDetails)
        .setOrException(MembersDetailsPage(eventType, 1), SampleData.memberDetails2)
      val pageNo = if(eventType == Event2) { 1 } else { 0 }
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe
        MembersDetailsPage(eventType, index = 0, memberPageNo = pageNo).route(EmptyWaypoints)
    }
  }

  private def testGetCorrectPageEvent10(): Unit = {
    s"must get the correct page for Event 10" in {
      val ua = UserAnswers()
        .setOrException(EventSelectionPage, EventSelection10)
      EventSelectionPage.nextPageNormalMode(EmptyWaypoints, ua).route(EmptyWaypoints) mustBe BecomeOrCeaseSchemePage.route(EmptyWaypoints)
    }
  }
}
