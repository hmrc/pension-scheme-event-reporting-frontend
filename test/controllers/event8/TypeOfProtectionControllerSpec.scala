/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.event8

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event8.TypeOfProtectionFormProvider
import models.UserAnswers
import models.enumeration.EventType
import models.event8.TypeOfProtection
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.event8.TypeOfProtectionPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event8.TypeOfProtectionView

import scala.concurrent.Future

class TypeOfProtectionControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val event8 = EventType.Event8
  private val event8a = EventType.Event8A

  private val formProvider = new TypeOfProtectionFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRouteEvent8: String = routes.TypeOfProtectionController.onPageLoad(waypoints, event8, 0).url

  private def postRouteEvent8: String = routes.TypeOfProtectionController.onSubmit(waypoints, event8, 0).url

  private def getRouteEvent8A: String = routes.TypeOfProtectionController.onPageLoad(waypoints, event8a, 0).url

  private def postRouteEvent8A: String = routes.TypeOfProtectionController.onSubmit(waypoints, event8a, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "TypeOfProtection Controller for Event 8" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteEvent8)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TypeOfProtectionView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, event8, 0)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers().set(TypeOfProtectionPage(event8, 0), TypeOfProtection.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteEvent8)

        val view = application.injector.instanceOf[TypeOfProtectionView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form.fill(TypeOfProtection.values.head), waypoints, event8, 0)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRouteEvent8).withFormUrlEncodedBody(("value", TypeOfProtection.values.head.toString))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(TypeOfProtectionPage(event8, index = 0), TypeOfProtection.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TypeOfProtectionPage(event8, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRouteEvent8).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[TypeOfProtectionView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces() mustEqual view(boundForm, waypoints, event8, 0)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }
  }

  "TypeOfProtection Controller for Event 8A" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteEvent8A)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TypeOfProtectionView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, event8a, 0)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers().set(TypeOfProtectionPage(event8a, 0), TypeOfProtection.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteEvent8A)

        val view = application.injector.instanceOf[TypeOfProtectionView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form.fill(TypeOfProtection.values.head), waypoints, event8a, 0)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRouteEvent8A).withFormUrlEncodedBody(("value", TypeOfProtection.values.head.toString))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(TypeOfProtectionPage(event8a, index = 0), TypeOfProtection.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TypeOfProtectionPage(event8a, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRouteEvent8A).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[TypeOfProtectionView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces() mustEqual view(boundForm, waypoints, event8a, 0)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }
  }
}
