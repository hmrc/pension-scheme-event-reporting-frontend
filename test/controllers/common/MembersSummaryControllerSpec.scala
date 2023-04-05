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
import models.enumeration.EventType.{Event22, Event23, Event4, Event5, Event6, Event8, Event8A}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.common.MembersSummaryPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import viewmodels.{Message, SummaryListRowWithTwoValues}
import views.html.common.MembersSummaryView

import scala.concurrent.Future

class MembersSummaryControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new MembersSummaryFormProvider()
  private val formEvent4 = formProvider(Event4)
  private val formEvent5 = formProvider(Event5)
  private val formEvent6 = formProvider(Event6)
  private val formEvent8 = formProvider(Event8)
  private val formEvent8a = formProvider(Event8A)
  private val formEvent22 = formProvider(Event22)
  private val formEvent23 = formProvider(Event23)
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRouteEvent4: String = routes.MembersSummaryController.onPageLoad(waypoints, Event4).url

  private def postRouteEvent4: String = routes.MembersSummaryController.onSubmit(waypoints, Event4).url

  private def getRouteEvent5: String = routes.MembersSummaryController.onPageLoad(waypoints, Event5).url

  private def postRouteEvent5: String = routes.MembersSummaryController.onSubmit(waypoints, Event5).url

  private def getRouteEvent6: String = routes.MembersSummaryController.onPageLoad(waypoints, Event6).url

  private def postRouteEvent6: String = routes.MembersSummaryController.onSubmit(waypoints, Event6).url

  private def getRouteEvent8: String = routes.MembersSummaryController.onPageLoad(waypoints, Event8).url

  private def postRouteEvent8: String = routes.MembersSummaryController.onSubmit(waypoints, Event8).url

  private def getRouteEvent8a: String = routes.MembersSummaryController.onPageLoad(waypoints, Event8A).url

  private def postRouteEvent8a: String = routes.MembersSummaryController.onSubmit(waypoints, Event8A).url

  private def getRouteEvent22: String = routes.MembersSummaryController.onPageLoad(waypoints, Event22).url

  private def postRouteEvent22: String = routes.MembersSummaryController.onSubmit(waypoints, Event22).url

  private def getRouteEvent23: String = routes.MembersSummaryController.onPageLoad(waypoints, Event23).url

  private def postRouteEvent23: String = routes.MembersSummaryController.onSubmit(waypoints, Event23).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "AnnualAllowanceSummary Controller" - {

    "Event 4" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent4and5(Event4))).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent4)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MembersSummaryView]

          val expectedSeq =
            Seq(
              SummaryListRowWithTwoValues(
                key = SampleData.memberDetails.fullName,
                firstValue = SampleData.memberDetails.nino,
                secondValue = SampleData.paymentDetailsCommon.amountPaid.toString(),
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      content = Text(Message("site.view")),
                      href = controllers.event4.routes.Event4CheckYourAnswersController.onPageLoad(0).url
                    ),
                    ActionItem(
                      content = Text(Message("site.remove")),
                      href = "#"
                    )
                  )
                ))
              ))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent4, waypoints, Event4, expectedSeq, "54.23", "2023")(request, messages(application)).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent4).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswersWithTaxYear.set(MembersSummaryPage(Event4, 1), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersSummaryPage(Event4, 1).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent4).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[MembersSummaryView]
          val boundForm = formEvent4.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event4, Nil, "0.00", "2023")(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
        }
      }
    }

    "Event 5" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent4and5(Event5))).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent5)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MembersSummaryView]

          val expectedSeq =
            Seq(
              SummaryListRowWithTwoValues(
                key = SampleData.memberDetails.fullName,
                firstValue = SampleData.memberDetails.nino,
                secondValue = SampleData.paymentDetailsCommon.amountPaid.toString(),
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      content = Text(Message("site.view")),
                      href = controllers.event5.routes.Event5CheckYourAnswersController.onPageLoad(0).url
                    ),
                    ActionItem(
                      content = Text(Message("site.remove")),
                      href = "#"
                    )
                  )
                ))
              ))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent5, waypoints, Event5, expectedSeq, "54.23", "2023")(request, messages(application)).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent5).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswersWithTaxYear.set(MembersSummaryPage(Event5, 1), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersSummaryPage(Event5, 1).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent5).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[MembersSummaryView]
          val boundForm = formEvent5.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event5, Nil, "0.00", "2023")(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
        }
      }
    }

    "Event 6" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent6)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent6)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MembersSummaryView]

          val expectedSeq =
            Seq(
              SummaryListRowWithTwoValues(
                key = SampleData.memberDetails.fullName,
                firstValue = SampleData.memberDetails.nino,
                secondValue = SampleData.crystallisedDetails.amountCrystallised.toString(),
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      content = Text(Message("site.view")),
                      href = controllers.event6.routes.Event6CheckYourAnswersController.onPageLoad(0).url
                    ),
                    ActionItem(
                      content = Text(Message("site.remove")),
                      href = "#"
                    )
                  )
                ))
              ))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent6, waypoints, Event6, expectedSeq, "857.12", "2023")(request, messages(application)).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent6).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswersWithTaxYear.set(MembersSummaryPage(Event6, 1), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersSummaryPage(Event6, 1).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent6).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[MembersSummaryView]
          val boundForm = formEvent6.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event6, Nil, "0.00", "2023")(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
        }
      }
    }

    "Event 8" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent8)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent8)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MembersSummaryView]

          val expectedSeq =
            Seq(
              SummaryListRowWithTwoValues(
                key = SampleData.memberDetails.fullName,
                firstValue = SampleData.memberDetails.nino,
                secondValue = SampleData.lumpSumDetails.lumpSumAmount.toString(),
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      content = Text(Message("site.view")),
                      href = controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(0).url
                    ),
                    ActionItem(
                      content = Text(Message("site.remove")),
                      href = "#"
                    )
                  )
                ))
              ))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent8, waypoints, Event8, expectedSeq, "223.11", "2023")(request, messages(application)).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent8).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswersWithTaxYear.set(MembersSummaryPage(Event8, 1), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersSummaryPage(Event8, 1).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent8).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[MembersSummaryView]
          val boundForm = formEvent8.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event8, Nil, "0.00", "2023")(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
        }
      }
    }

    "Event 8A" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent8A)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent8a)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MembersSummaryView]

          val expectedSeq =
            Seq(
              SummaryListRowWithTwoValues(
                key = SampleData.memberDetails.fullName,
                firstValue = SampleData.memberDetails.nino,
                secondValue = SampleData.lumpSumDetails.lumpSumAmount.toString(),
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      content = Text(Message("site.view")),
                      href = controllers.event8a.routes.Event8ACheckYourAnswersController.onPageLoad(0).url
                    ),
                    ActionItem(
                      content = Text(Message("site.remove")),
                      href = "#"
                    )
                  )
                ))
              ))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent8a, waypoints, Event8A, expectedSeq, "223.11", "2023")(request, messages(application)).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent8a).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswersWithTaxYear.set(MembersSummaryPage(Event8A, 1), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersSummaryPage(Event8A, 1).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent8a).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[MembersSummaryView]
          val boundForm = formEvent8a.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event8A, Nil, "0.00", "2023")(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
        }
      }
    }

    "Event 22" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent22)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent22)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MembersSummaryView]

          val expectedSeq =
            Seq(
              SummaryListRowWithTwoValues(
                key = SampleData.memberDetails.fullName,
                firstValue = SampleData.memberDetails.nino,
                secondValue = SampleData.totalPaymentAmount.toString(),
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      content = Text(Message("site.view")),
                      href = controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(0).url
                    ),
                    ActionItem(
                      content = Text(Message("site.remove")),
                      href = "#"
                    )
                  )
                ))
              ))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent22, waypoints, Event22, expectedSeq, "999.11", "2023")(request, messages(application)).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent22).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswersWithTaxYear.set(MembersSummaryPage(Event22, 1), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersSummaryPage(Event22, 1).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent22).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[MembersSummaryView]
          val boundForm = formEvent22.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event22, Nil, "0.00", "2023")(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
        }
      }
    }

    "Event 23" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent23)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent23)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MembersSummaryView]

          val expectedSeq =
            Seq(
              SummaryListRowWithTwoValues(
                key = SampleData.memberDetails.fullName,
                firstValue = SampleData.memberDetails.nino,
                secondValue = SampleData.totalPaymentAmountEvent23CurrencyFormat,
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      content = Text(Message("site.view")),
                      href = controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(0).url
                    ),
                    ActionItem(
                      content = Text(Message("site.remove")),
                      href = "#"
                    )
                  )
                ))
              ))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent23, waypoints, Event23, expectedSeq, "1,234.56", "2023")(request, messages(application)).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent23).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswersWithTaxYear.set(MembersSummaryPage(Event23, 1), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersSummaryPage(Event23, 1).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent23).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[MembersSummaryView]
          val boundForm = formEvent23.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event23, Nil, "0.00", "2023")(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
        }
      }
    }
  }
}
