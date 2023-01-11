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
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.common.ManualOrUploadPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.common.ManualOrUploadView

class ManualOrUploadControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val formProvider = new ManualOrUploadFormProvider()

  "ManualOrUploadController" - {
    "event1" - {
      val event1 = EventType.Event1
      val form = formProvider(event1)

      def getRoute: String = routes.ManualOrUploadController.onPageLoad(waypoints, event1, 0).url

      def postRoute: String = routes.ManualOrUploadController.onSubmit(waypoints, event1, 0).url

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ManualOrUploadView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, waypoints, event1, 0)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ManualOrUpload.values.head.toString))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(ManualOrUploadPage(event1, 0), ManualOrUpload.values.head).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual ManualOrUploadPage(event1, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[ManualOrUploadView]
          val boundForm = form.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, event1, 0)(request, messages(application)).toString
        }
      }
    }
    "event22" - {
      val event22 = EventType.Event22
      val form = formProvider(event22)

      def getRoute: String = routes.ManualOrUploadController.onPageLoad(waypoints, event22, 0).url

      def postRoute: String = routes.ManualOrUploadController.onSubmit(waypoints, event22, 0).url

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ManualOrUploadView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, waypoints, event22, 0)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ManualOrUpload.values.head.toString))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(ManualOrUploadPage(event22, 0), ManualOrUpload.values.head).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual ManualOrUploadPage(event22, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[ManualOrUploadView]
          val boundForm = form.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, event22, 0)(request, messages(application)).toString
        }
      }
    }
    "event23" - {
      val event23 = EventType.Event23
      val form = formProvider(event23)

      def getRoute: String = routes.ManualOrUploadController.onPageLoad(waypoints, event23, 0).url

      def postRoute: String = routes.ManualOrUploadController.onSubmit(waypoints, event23, 0).url

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ManualOrUploadView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, waypoints, event23, 0)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ManualOrUpload.values.head.toString))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(ManualOrUploadPage(event23, 0), ManualOrUpload.values.head).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual ManualOrUploadPage(event23, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[ManualOrUploadView]
          val boundForm = form.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, event23, 0)(request, messages(application)).toString
        }
      }
    }
  }
}
