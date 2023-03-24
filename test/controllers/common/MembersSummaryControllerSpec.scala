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
import data.SampleData.{sampleMemberJourneyDataEvent22, sampleMemberJourneyDataEvent23}
import forms.common.MembersSummaryFormProvider
import models.enumeration.EventType.{Event22, Event23}
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
  private val formEvent22 = formProvider(Event22)
  private val formEvent23 = formProvider(Event23)
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

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
          contentAsString(result) mustEqual view(formEvent22, waypoints, Event22, expectedSeq, "999.11", "")(request, messages(application)).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent22).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(MembersSummaryPage(Event22), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersSummaryPage(Event22).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent22).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[MembersSummaryView]
          val boundForm = formEvent22.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event22, Nil, "0.00", "")(request, messages(application)).toString
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
          contentAsString(result) mustEqual view(formEvent23, waypoints, Event23, expectedSeq, "1,234.56", "")(request, messages(application)).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent23).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(MembersSummaryPage(Event23), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersSummaryPage(Event23).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent23).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[MembersSummaryView]
          val boundForm = formEvent23.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event23, Nil, "0.00", "")(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
        }
      }
    }
  }
}
