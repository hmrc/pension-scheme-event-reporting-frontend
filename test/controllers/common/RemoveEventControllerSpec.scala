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
import data.SampleData.sampleEvent18JourneyData
import forms.common.RemoveEventFormProvider
import models.enumeration.EventType.Event18
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import pages.common.RemoveEventPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CompileService
import views.html.common.RemoveEventView

class RemoveEventControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new RemoveEventFormProvider()
  private val form = formProvider(Event18)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockCompileService = mock[CompileService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[CompileService].toInstance(mockCompileService)
  )

  private def getRoute: String = routes.RemoveEventController.onPageLoad(waypoints, Event18).url

  private def postRoute: String = routes.RemoveEventController.onSubmit(waypoints, Event18).url

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
    reset(mockCompileService)
  }

  "RemoveEvent Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[RemoveEventView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(form, waypoints, Event18, request, messages(application)).toString
      }
    }

    "must not change anything in userAnswer and not call user-cache connector when selecting false" in {
      val application = applicationBuilder(userAnswers = Some(sampleEvent18JourneyData), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "false"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RemoveEventPage(Event18).navigate(waypoints, sampleEvent18JourneyData, sampleEvent18JourneyData).url
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))
        val view = application.injector.instanceOf[RemoveEventView]
        val boundForm = form.bind(Map("value" -> "invalid"))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, Event18)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
