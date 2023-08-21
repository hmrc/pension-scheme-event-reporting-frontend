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
import forms.common.ManualOrUploadFormProvider
import models.common.ManualOrUpload
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event23, Event6}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.common.ManualOrUploadPage
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.common.ManualOrUploadView

class ManualOrUploadControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val seqOfEvents = Seq(Event1, Event6, Event22, Event23)
  private val waypoints = EmptyWaypoints
  private val manualOrUploadFormProvider = new ManualOrUploadFormProvider()

  private def form(eventType: EventType): Form[ManualOrUpload] = manualOrUploadFormProvider(eventType)

  private def getRoute(eventType: EventType): String = routes.ManualOrUploadController.onPageLoad(waypoints, eventType, 0).url

  private def postRoute(eventType: EventType): String = routes.ManualOrUploadController.onSubmit(waypoints, eventType, 0).url

  "ManualOrUploadController" - {
    for (event <- seqOfEvents) {
      testReturnOkAndCorrectView(event)
      testSaveAnswerAndRedirectWhenValid(event)
      testBadRequestForInvalidDataSubmission(event)
    }
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManualOrUploadView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(form(eventType), waypoints, eventType, index = 0, request, messages(application)).toString
      }
    }
  }

  private def testSaveAnswerAndRedirectWhenValid(eventType: EventType): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", ManualOrUpload.values.head.toString))
        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(ManualOrUploadPage(eventType, 0), ManualOrUpload.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ManualOrUploadPage(eventType, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
      }
    }
  }

  private def testBadRequestForInvalidDataSubmission(eventType: EventType): Unit = {
    s"must return bad request when invalid data is submitted for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[ManualOrUploadView]
        val boundForm = form(eventType).bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view.render(boundForm, waypoints, eventType, index = 0, request, messages(application)).toString
      }
    }
  }

}
