/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.routes
import forms.common.MembersDetailsFormProvider
import models.enumeration.EventType
import models.UserAnswers
import models.common.MembersDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.MockitoSugar.{mock, reset}
import org.scalatest.BeforeAndAfterEach
import pages.EmptyWaypoints
import pages.common.MembersDetailsPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.common.MembersDetailsView

import scala.concurrent.Future


class MembersDetailsControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints
  private val event1 = EventType.Event1
  private val event23 = EventType.Event23


  private val formProvider = new MembersDetailsFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRouteEvent1: String = routes.MembersDetailsController.onPageLoad(waypoints, event1).url
  private def postRouteEvent1: String = routes.MembersDetailsController.onSubmit(waypoints, event1).url
  private def getRouteEvent23: String = routes.MembersDetailsController.onPageLoad(waypoints, event23).url
  private def postRouteEvent23: String = routes.MembersDetailsController.onSubmit(waypoints, event23).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validValue = MembersDetails("Joe", "Blogs", "AA123456D")

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "MembersDetails Controller" - {
    "event 1" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent1)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MembersDetailsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, waypoints, event1)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers().set(MembersDetailsPage(event1), validValue).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent1)

          val view = application.injector.instanceOf[MembersDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validValue), waypoints, event1)(request, messages(application)).toString
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
            FakeRequest(POST, postRouteEvent1).withFormUrlEncodedBody(("firstName", validValue.firstName), ("lastName", validValue.lastName), ("nino", validValue.nino))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(MembersDetailsPage(event1), validValue).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersDetailsPage(event1).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
          verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
        }
      }

      "must return bad request when invalid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent1).withFormUrlEncodedBody(("firstName", "%"), ("lastName", ""), ("nino", "abc"))

          val view = application.injector.instanceOf[MembersDetailsView]
          val boundForm = form.bind(Map("firstName" -> "%", "lastName" -> "", "nino" -> "abc"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, event1)(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
        }
      }

      // TODO These tests should be enabled when we have redirect to JourneyRecoveryController functionality
      /*    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute)
            .withFormUrlEncodedBody(("firstName", validValue.firstName), ("lastName", validValue.lastName), ("nino", validValue.nino))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }*/
    }
    "event23" -{

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent23)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MembersDetailsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, waypoints, event23)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers().set(MembersDetailsPage(event23), validValue).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent23)

          val view = application.injector.instanceOf[MembersDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validValue), waypoints, event23)(request, messages(application)).toString
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
            FakeRequest(POST, postRouteEvent23).withFormUrlEncodedBody(("firstName", validValue.firstName), ("lastName", validValue.lastName), ("nino", validValue.nino))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(MembersDetailsPage(event23), validValue).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersDetailsPage(event23).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
          verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
        }
      }

      "must return bad request when invalid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent23).withFormUrlEncodedBody(("firstName", "%"), ("lastName", ""), ("nino", "abc"))

          val view = application.injector.instanceOf[MembersDetailsView]
          val boundForm = form.bind(Map("firstName" -> "%", "lastName" -> "", "nino" -> "abc"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, event23)(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
        }
      }

      // TODO These tests should be enabled when we have redirect to JourneyRecoveryController functionality
      /*    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute)
            .withFormUrlEncodedBody(("firstName", validValue.firstName), ("lastName", validValue.lastName), ("nino", validValue.nino))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }*/
    }
  }
}
