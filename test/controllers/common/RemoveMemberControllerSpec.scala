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
import data.SampleData.{sampleMemberJourneyDataEvent1, sampleMemberJourneyDataEvent3and4and5}
import forms.common.RemoveMemberFormProvider
import models.enumeration.EventType.{Event1, Event5}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import pages.common.RemoveMemberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.common.RemoveMemberView

import scala.concurrent.Future

class RemoveMemberControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new RemoveMemberFormProvider()
  private val formEvent1 = formProvider("unauthorised payment")
  private val formEvent5 = formProvider("cessation of ill-health pension")

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRouteForEvent1: String = routes.RemoveMemberController.onPageLoad(waypoints, Event1, 0).url

  private def getRoute: String = routes.RemoveMemberController.onPageLoad(waypoints, Event5, 0).url

  private def postRouteForEvent1: String = routes.RemoveMemberController.onSubmit(waypoints, Event1, 0).url

  private def postRoute: String = routes.RemoveMemberController.onSubmit(waypoints, Event5, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "RemoveMember Controller" - {


    "must return OK and the correct view for a GET in event 1" in {

      val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent1)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteForEvent1)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveMemberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formEvent1, waypoints, Event1, "unauthorised payment", 0)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET in an event that isn't event 1" in {

      val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent3and4and5(Event5))).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveMemberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formEvent5, waypoints, Event5, "cessation of ill-health pension", 0)(request, messages(application)).toString
      }
    }


    "must populate the view correctly on a GET when the question has previously been answered for event 1" in {

      val userAnswers = sampleMemberJourneyDataEvent1.set(RemoveMemberPage(Event1, 0), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteForEvent1)

        val view = application.injector.instanceOf[RemoveMemberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formEvent1.fill(true), waypoints, Event1, "unauthorised payment", 0)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered in an event that isn't event 1" in {

      val userAnswers = sampleMemberJourneyDataEvent3and4and5(Event5).set(RemoveMemberPage(Event5, 0), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[RemoveMemberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formEvent5.fill(true), waypoints, Event5, "cessation of ill-health pension", 0)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted  for event 1" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent1), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRouteForEvent1).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(RemoveMemberPage(Event1, 0), true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RemoveMemberPage(Event1, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted in an event that isn't event 1" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent3and4and5(Event5)), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(RemoveMemberPage(Event5, 0), true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RemoveMemberPage(Event5, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted for event 1" in {
      val application =
        applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent1), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRouteForEvent1).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[RemoveMemberView]
        val boundForm = formEvent1.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, Event1, "unauthorised payment", 0)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted in an event that isn't event 1" in {
      val application =
        applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent3and4and5(Event5)), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[RemoveMemberView]
        val boundForm = formEvent5.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, Event5, "cessation of ill-health pension", 0)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
