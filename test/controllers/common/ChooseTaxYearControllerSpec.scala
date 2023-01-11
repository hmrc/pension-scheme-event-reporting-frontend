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
import forms.common.ChooseTaxYearFormProvider
import models.UserAnswers
import models.common.ChooseTaxYear
import models.enumeration.EventType.{Event22, Event23}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, common}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.DateHelper
import views.html.common.ChooseTaxYearView

import java.time.LocalDate
import scala.concurrent.Future

class ChooseTaxYearControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new ChooseTaxYearFormProvider()
  private val formEvent23 = formProvider(Event23)
  private val formEvent22 = formProvider(Event22)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRouteEvent23: String = routes.ChooseTaxYearController.onPageLoad(waypoints, Event23, 0).url

  private def postRouteEvent23: String = routes.ChooseTaxYearController.onSubmit(waypoints, Event23, 0).url

  private def getRouteEvent22: String = routes.ChooseTaxYearController.onPageLoad(waypoints, Event22, 0).url

  private def postRouteEvent22: String = routes.ChooseTaxYearController.onSubmit(waypoints, Event22, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    DateHelper.setDate(Some(LocalDate.of(2015, 6, 1)))

    reset(mockUserAnswersCacheConnector)
  }

  "ChooseTaxYear Controller" - {
    "Event 23" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent23)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ChooseTaxYearView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent23, waypoints, Event23, 0)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers().set(common.ChooseTaxYearPage(Event23, 0), ChooseTaxYear.values.head).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent23)

          val view = application.injector.instanceOf[ChooseTaxYearView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent23.fill(ChooseTaxYear.values.head), waypoints, Event23, 0)(request, messages(application)).toString
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
            FakeRequest(POST, postRouteEvent23).withFormUrlEncodedBody(("value", ChooseTaxYear.values.head.toString))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(common.ChooseTaxYearPage(Event23, 0), ChooseTaxYear.values.head).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual common.ChooseTaxYearPage(Event23, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
          verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
        }
      }

      "must return bad request when invalid data is submitted" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent23).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[ChooseTaxYearView]
          val boundForm = formEvent23.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event23, 0)(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
        }
      }
    }
    "Event 22" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent22)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ChooseTaxYearView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent22, waypoints, Event22, 0)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = UserAnswers().set(common.ChooseTaxYearPage(Event22, 0), ChooseTaxYear.values.head).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent22)

          val view = application.injector.instanceOf[ChooseTaxYearView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent22.fill(ChooseTaxYear.values.head), waypoints, Event22, 0)(request, messages(application)).toString
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
            FakeRequest(POST, postRouteEvent22).withFormUrlEncodedBody(("value", ChooseTaxYear.values.head.toString))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(common.ChooseTaxYearPage(Event22, 0), ChooseTaxYear.values.head).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual common.ChooseTaxYearPage(Event22, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
          verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
        }
      }

      "must return bad request when invalid data is submitted" in {
        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent22).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[ChooseTaxYearView]
          val boundForm = formEvent22.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event22, 0)(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
        }
      }
    }
  }
}
