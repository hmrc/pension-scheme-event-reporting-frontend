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
import forms.common.TotalPensionAmountsFormProvider
import models.UserAnswers
import models.enumeration.EventType
import org.apache.commons.lang3.StringUtils
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.common.TotalPensionAmountsPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.common.TotalPensionAmountsView

import scala.concurrent.Future

class TotalPensionAmountsControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val event23 = EventType.Event23
  private val event22 = EventType.Event22
  private val formProvider = new TotalPensionAmountsFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRouteEvent23: String = routes.TotalPensionAmountsController.onPageLoad(waypoints, event23).url

  private def postRouteEvent23: String = routes.TotalPensionAmountsController.onSubmit(waypoints, event23).url

  private def getRouteEvent22: String = routes.TotalPensionAmountsController.onPageLoad(waypoints, event22).url

  private def postRouteEvent22: String = routes.TotalPensionAmountsController.onSubmit(waypoints, event22).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validValue = BigDecimal(1000.00)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "MembersTotalPensionAmounts Controller" - {

    "event23" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent23)

          val result = route(application, request).value

          val view = application.injector.instanceOf[TotalPensionAmountsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, waypoints, event23, StringUtils.EMPTY)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers().set(TotalPensionAmountsPage(event23), validValue).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent23)

          val view = application.injector.instanceOf[TotalPensionAmountsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validValue), waypoints, event23, StringUtils.EMPTY)(request, messages(application)).toString
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
            FakeRequest(POST, postRouteEvent23).withFormUrlEncodedBody(("value", "33.00"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(TotalPensionAmountsPage(event23), validValue).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual TotalPensionAmountsPage(event23).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
          verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
        }
      }

      "must return bad request when invalid data is submitted" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent23).withFormUrlEncodedBody(("value", ""))

          val view = application.injector.instanceOf[TotalPensionAmountsView]
          val boundForm = form.bind(Map("value" -> ""))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, event23, StringUtils.EMPTY)(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
        }
      }
    }
    "event22" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent22)

          val result = route(application, request).value

          val view = application.injector.instanceOf[TotalPensionAmountsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, waypoints, event22, StringUtils.EMPTY)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers().set(TotalPensionAmountsPage(event22), validValue).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent22)

          val view = application.injector.instanceOf[TotalPensionAmountsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validValue), waypoints, event22, StringUtils.EMPTY)(request, messages(application)).toString
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
            FakeRequest(POST, postRouteEvent22).withFormUrlEncodedBody(("value", "33.00"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(TotalPensionAmountsPage(event22), validValue).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual TotalPensionAmountsPage(event22).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
          verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
        }
      }

      "must return bad request when invalid data is submitted" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent22).withFormUrlEncodedBody(("value", ""))

          val view = application.injector.instanceOf[TotalPensionAmountsView]
          val boundForm = form.bind(Map("value" -> ""))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, event22, StringUtils.EMPTY)(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
        }
      }
    }
  }
}
