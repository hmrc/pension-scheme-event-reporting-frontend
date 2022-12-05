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

package controllers.event22

import base.SpecBase
import forms.event22.HowAddAnnualAllowanceFormProvider
import models.event22.HowAddAnnualAllowance
import org.scalatest.BeforeAndAfterEach
import pages.EmptyWaypoints
import pages.event22.HowAddAnnualAllowancePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event22.HowAddAnnualAllowanceView

class HowAddAnnualAllowanceControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new HowAddAnnualAllowanceFormProvider()
  private val form = formProvider()

  private def getRoute: String = routes.HowAddAnnualAllowanceController.onPageLoad(waypoints, 0).url

  private def postRoute: String = routes.HowAddAnnualAllowanceController.onSubmit(waypoints, 0).url

  "Test Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowAddAnnualAllowanceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, 0)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", HowAddAnnualAllowance.values.head.toString))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(HowAddAnnualAllowancePage(0), HowAddAnnualAllowance.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual HowAddAnnualAllowancePage(0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[HowAddAnnualAllowanceView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, 0)(request, messages(application)).toString
      }
    }
  }
}
