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

package controllers.event23

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event23.HowAddDualAllowanceFormProvider
import models.UserAnswers
import models.event23.HowAddDualAllowance
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.MockitoSugar.{mock, reset}
import org.scalatest.BeforeAndAfterEach
import pages.EmptyWaypoints
import pages.event23.HowAddDualAllowancePage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event23.HowAddDualAllowanceView

import scala.concurrent.Future

class HowAddDualAllowanceControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new HowAddDualAllowanceFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

<<<<<<< HEAD:test/controllers/event1/MembersDetailsControllerSpec.scala
  private def getRoute: String = routes.MembersDetailsController.onPageLoad(waypoints, 0).url

  private def postRoute: String = routes.MembersDetailsController.onSubmit(waypoints, 0).url
=======
  private def getRoute: String = routes.HowAddDualAllowanceController.onPageLoad(waypoints).url

  private def postRoute: String = routes.HowAddDualAllowanceController.onSubmit(waypoints).url
>>>>>>> c82eb80c87950d8ee4d4965fce60d2ec0337ed20:test/controllers/event23/HowAddDualAllowanceControllerSpec.scala

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "Test Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowAddDualAllowanceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, 0)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

<<<<<<< HEAD:test/controllers/event1/MembersDetailsControllerSpec.scala
      val userAnswers = UserAnswers().set(MembersDetailsPage(0), validValue).success.value
=======
      val userAnswers = UserAnswers().set(HowAddDualAllowancePage, HowAddDualAllowance.values.head).success.value
>>>>>>> c82eb80c87950d8ee4d4965fce60d2ec0337ed20:test/controllers/event23/HowAddDualAllowanceControllerSpec.scala

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[HowAddDualAllowanceView]

        val result = route(application, request).value

        status(result) mustEqual OK
<<<<<<< HEAD:test/controllers/event1/MembersDetailsControllerSpec.scala
        contentAsString(result) mustEqual view(form.fill(validValue), waypoints, 0)(request, messages(application)).toString
=======
        contentAsString(result) mustEqual view(form.fill(HowAddDualAllowance.values.head), waypoints)(request, messages(application)).toString
>>>>>>> c82eb80c87950d8ee4d4965fce60d2ec0337ed20:test/controllers/event23/HowAddDualAllowanceControllerSpec.scala
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
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", HowAddDualAllowance.values.head.toString))

        val result = route(application, request).value
<<<<<<< HEAD:test/controllers/event1/MembersDetailsControllerSpec.scala
        val updatedAnswers = emptyUserAnswers.set(MembersDetailsPage(0), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MembersDetailsPage(0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
=======
        val updatedAnswers = emptyUserAnswers.set(HowAddDualAllowancePage, HowAddDualAllowance.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual HowAddDualAllowancePage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
>>>>>>> c82eb80c87950d8ee4d4965fce60d2ec0337ed20:test/controllers/event23/HowAddDualAllowanceControllerSpec.scala
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[HowAddDualAllowanceView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, 0)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
