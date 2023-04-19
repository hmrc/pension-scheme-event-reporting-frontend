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

package controllers.common

import base.SpecBase
import connectors.UserAnswersCacheConnector
import data.SampleData
import data.SampleData._
import forms.common.MembersSummaryFormProvider
import models.UserAnswers
import models.enumeration.EventType
import models.enumeration.EventType.{Event2, Event22, Event23, Event3, Event4, Event5, Event6, Event8, Event8A}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.common.MembersSummaryPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.EventPaginationService
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import viewmodels.{Message, SummaryListRowWithTwoValues}
import views.html.common.{MembersSummaryView, MembersSummaryViewWithPagination}

import scala.concurrent.Future

class MembersSummaryControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new MembersSummaryFormProvider()
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val mockEventPaginationService = mock[EventPaginationService]

  private def getRoute(eventType: EventType): String = routes.MembersSummaryController.onPageLoad(waypoints, eventType).url

  private def getRouteWithPagination(eventType: EventType): String = routes.MembersSummaryController.onPageLoadWithPageNumber(waypoints, eventType, 0).url

  private def postRoute(eventType: EventType): String = routes.MembersSummaryController.onSubmit(waypoints, eventType).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
    reset(mockEventPaginationService)
  }

  "MembersSummary Controller" - {
    testSuite(formProvider(Event2), Event2, sampleMemberJourneyDataEvent2, SampleData.amountPaid.toString(),
      controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(0).url, "999.11")

    testSuite(formProvider(Event3), Event3, sampleMemberJourneyDataEvent3and4and5(Event3), SampleData.paymentDetailsCommon.amountPaid.toString(),
      controllers.event3.routes.Event3CheckYourAnswersController.onPageLoad(0).url, "54.23")

    testSuite(formProvider(Event4), Event4, sampleMemberJourneyDataEvent3and4and5(Event4), SampleData.paymentDetailsCommon.amountPaid.toString(),
      controllers.event4.routes.Event4CheckYourAnswersController.onPageLoad(0).url, "54.23")

    testSuite(formProvider(Event5), Event5, sampleMemberJourneyDataEvent3and4and5(Event5), SampleData.paymentDetailsCommon.amountPaid.toString(),
      controllers.event5.routes.Event5CheckYourAnswersController.onPageLoad(0).url, "54.23")

    testSuite(formProvider(Event6), Event6, sampleMemberJourneyDataEvent6, SampleData.crystallisedDetails.amountCrystallised.toString(),
      controllers.event6.routes.Event6CheckYourAnswersController.onPageLoad(0).url, "857.12")

    testSuite(formProvider(Event8), Event8, sampleMemberJourneyDataEvent8, SampleData.lumpSumDetails.lumpSumAmount.toString(),
      controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(0).url, "223.11")

    testSuite(formProvider(Event8A), Event8A, sampleMemberJourneyDataEvent8A, SampleData.lumpSumDetails.lumpSumAmount.toString(),
      controllers.event8a.routes.Event8ACheckYourAnswersController.onPageLoad(0).url, "223.11")

    testSuite(formProvider(Event22), Event22, sampleMemberJourneyDataEvent22, SampleData.totalPaymentAmount.toString(),
      controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(0).url, "999.11")

    testSuite(formProvider(Event23), Event23, sampleMemberJourneyDataEvent23, SampleData.totalPaymentAmountEvent23CurrencyFormat,
      controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(0).url, "1,234.56")
  }

  private def testSuite(form: Form[Boolean], eventType: EventType, sampleData: UserAnswers, secondValue: String, href: String, arbitraryAmount: String): Unit = {
    testReturnOkAndCorrectView(eventType, form, sampleData, secondValue, href, arbitraryAmount)
    testSaveAnswerAndRedirectWhenValid(eventType)
    testBadRequestForInvalidDataSubmission(eventType, form)
  }

  private def testReturnOkAndCorrectView(eventType: EventType, form: Form[Boolean], sampleData: UserAnswers, secondValue: String, href: String, arbitraryAmount: String): Unit = {
    s"must return OK and the correct view for a GET for Event $eventType" in {

      val application = applicationBuilder(userAnswers = Some(sampleData)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersSummaryView]

        val expectedSeq =
          Seq(
            SummaryListRowWithTwoValues(
              key = SampleData.memberDetails.fullName,
              firstValue = SampleData.memberDetails.nino,
              secondValue = secondValue,
              actions = Some(Actions(
                items = Seq(
                  ActionItem(
                    content = Text(Message("site.view")),
                    href = href
                  ),
                  ActionItem(
                    content = Text(Message("site.remove")),
                    href = "#"
                  )
                )
              ))
            ))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, eventType, expectedSeq, arbitraryAmount, "2023")(request, messages(application)).toString
      }
    }
  }

//  private def testReturnOkAndCorrectViewWithPagination(eventType: EventType, form: Form[Boolean], sampleData: UserAnswers, secondValue: String, href: String, arbitraryAmount: String): Unit = {
//    s"must return OK and the correct view for a GET for Event $eventType" in {
//
//      val application = applicationBuilder(userAnswers = Some(sampleData)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, getRouteWithPagination(eventType))
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[MembersSummaryViewWithPagination]
//
//        val expectedSeq =
//          Seq(
//            SummaryListRowWithTwoValues(
//              key = SampleData.memberDetails.fullName,
//              firstValue = SampleData.memberDetails.nino,
//              secondValue = secondValue,
//              actions = Some(Actions(
//                items = Seq(
//                  ActionItem(
//                    content = Text(Message("site.view")),
//                    href = href
//                  ),
//                  ActionItem(
//                    content = Text(Message("site.remove")),
//                    href = "#"
//                  )
//                )
//              ))
//            ))
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(form, waypoints, eventType, expectedSeq, arbitraryAmount, "2023", 0)(request, messages(application)).toString
//      }
//    }
//  }


  private def testSaveAnswerAndRedirectWhenValid(eventType: EventType): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for Event $eventType" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswersWithTaxYear.set(MembersSummaryPage(eventType, 1), true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MembersSummaryPage(eventType, 1).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
      }
    }
  }

  private def testBadRequestForInvalidDataSubmission(eventType: EventType, form: Form[Boolean]): Unit = {
    s"must return bad request when invalid data is submitted for Event $eventType" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[MembersSummaryView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, eventType, Nil, "0.00", "2023")(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
      }
    }
  }
}
