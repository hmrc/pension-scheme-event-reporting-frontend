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
import connectors.UserAnswersCacheConnector
import forms.event22.HowAddAnnualAllowanceFormProvider
import models.UserAnswers
import models.event22.HowAddAnnualAllowance
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import pages.event22.HowAddAnnualAllowancePage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event22.HowAddAnnualAllowanceView

import scala.concurrent.Future

class HowAddAnnualAllowanceControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new HowAddAnnualAllowanceFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.HowAddAnnualAllowanceController.onPageLoad(waypoints).url

  private def postRoute: String = routes.HowAddAnnualAllowanceController.onSubmit(waypoints).url

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

        val view = application.injector.instanceOf[HowAddAnnualAllowanceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers().set(HowAddAnnualAllowancePage, HowAddAnnualAllowance.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[HowAddAnnualAllowanceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(HowAddAnnualAllowance.values.head), waypoints)(request, messages(application)).toString
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
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", HowAddAnnualAllowance.values.head.toString))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(HowAddAnnualAllowancePage, HowAddAnnualAllowance.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual HowAddAnnualAllowancePage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
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

        val view = application.injector.instanceOf[HowAddAnnualAllowanceView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
