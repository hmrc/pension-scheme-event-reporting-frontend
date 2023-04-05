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

package controllers

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.WantToSubmitFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, WantToSubmitPage}
import play.api.inject.guice.GuiceableModule
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WantToSubmitView

import scala.concurrent.Future

class WantToSubmitControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new WantToSubmitFormProvider()
  private val form = formProvider()

  private def getRoute: String = routes.WantToSubmitController.onPageLoad(waypoints).url
  private def postRoute: String = routes.WantToSubmitController.onSubmit(waypoints).url

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "WantToSubmit Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WantToSubmitView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers().set(WantToSubmitPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[WantToSubmitView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
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
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[WantToSubmitView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
      }
    }

    "must save the answer and redirect to next page on submit (when selecting YES)" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val userAnswerUpdated = UserAnswers().setOrException(WantToSubmitPage, true)

        status(result) mustEqual SEE_OTHER
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
        redirectLocation(result).value mustEqual WantToSubmitPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).url
      }
    }

    "must save the answer and redirect to next page on submit (when selecting NO)" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

        println("\n\n\n\n\n\n\n -----yyyyyyyyyyyyyyyy--------")

        println(s"\n\n\n\n result = : ${result.value} \n\n\n")
        println("\n\n\n\n\n\n\n ------qqqqqqqqqqqqq-------")

        val x = redirectLocation(result)
        println(s"x = ($x)")

        status(result) mustEqual SEE_OTHER
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
        redirectLocation(result).value mustEqual request.returnUrl
      }
    }
  }
}
