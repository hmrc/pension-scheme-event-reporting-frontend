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

package controllers.event1.member

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event1.member.ErrorDescriptionFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.event1.member.ErrorDescriptionPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event1.member.ErrorDescriptionView

import scala.concurrent.Future

class ErrorDescriptionControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val formProvider = new ErrorDescriptionFormProvider()
  private val form = formProvider()
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val validValue = "abc"

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private def getRoute: String = routes.ErrorDescriptionController.onPageLoad(waypoints, 0).url

  private def postRoute: String = routes.ErrorDescriptionController.onSubmit(waypoints, 0).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "ErrorDescription Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[ErrorDescriptionView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(form, waypoints, index = 0, request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers().set(ErrorDescriptionPage(0), validValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, getRoute)
        val view = application.injector.instanceOf[ErrorDescriptionView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(form.fill(validValue), waypoints, index = 0, request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted (non empty value)" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "abcdef"))
        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(ErrorDescriptionPage(0), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ErrorDescriptionPage(0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must return bad request when submitting an empty value" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must return bad request when submitting data that is too long" in {
      val invalidValue = "A" * 161

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      running(application) {
        val view = application.injector.instanceOf[ErrorDescriptionView]
        val boundForm = form.bind(Map("value" -> invalidValue))
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", invalidValue))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces()mustEqual view.render(boundForm, waypoints, index = 0, request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val invalidValue = "~|"

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      running(application) {
        val view = application.injector.instanceOf[ErrorDescriptionView]
        val boundForm = form.bind(Map("value" -> invalidValue))
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", invalidValue))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces()mustEqual view.render(boundForm, waypoints, index = 0, request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }
  }
}
