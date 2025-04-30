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

package controllers.event8

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event8.LumpSumAmountAndDateFormProvider
import models.UserAnswers
import models.enumeration.EventType.{Event8, Event8A}
import models.event8.LumpSumDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.event8.LumpSumAmountAndDatePage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event8.LumpSumAmountAndDateView

import java.time.LocalDate
import scala.concurrent.Future

class LumpSumAmountAndDateControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val stubMin: LocalDate = LocalDate.of(2006, 4, 6) //06-04-2006
  private val stubMax: LocalDate = LocalDate.now() //05-04-2023

  private val formProvider = new LumpSumAmountAndDateFormProvider()
  private val form = formProvider(stubMin, stubMax)
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val event8 = Event8
  private val event8a = Event8A

  private def getRouteEvent8: String = routes.LumpSumAmountAndDateController.onPageLoad(waypoints, event8, 0).url

  private def postRouteEvent8: String = routes.LumpSumAmountAndDateController.onSubmit(waypoints, event8, 0).url

  private def getRouteEvent8A: String = routes.LumpSumAmountAndDateController.onPageLoad(waypoints, event8a, 0).url

  private def postRouteEvent8A: String = routes.LumpSumAmountAndDateController.onSubmit(waypoints, event8a, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validValue = LumpSumDetails(1000.00, LocalDate.of(2022, 7, 12))

  import LumpSumAmountAndDateControllerSpec._

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "LumpSumAmountAndDate Controller for Event 8" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteEvent8)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LumpSumAmountAndDateView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, event8, 0, stubMin, stubMax)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers().set(LumpSumAmountAndDatePage(event8, 0), validValue).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteEvent8)

        val view = application.injector.instanceOf[LumpSumAmountAndDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form.fill(validValue), waypoints, event8, 0, stubMin, stubMax)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRouteEvent8).withFormUrlEncodedBody(lumpSumDetails("1000.00", Some(validDate))*)

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(LumpSumAmountAndDatePage(event8, 0), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual LumpSumAmountAndDatePage(event8, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRouteEvent8).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[LumpSumAmountAndDateView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces() mustEqual view(boundForm, waypoints, event8, 0, stubMin, stubMax)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }
  }

  "LumpSumAmountAndDate Controller for Event 8A" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteEvent8A)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LumpSumAmountAndDateView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view(form, waypoints, event8a, 0, stubMin, stubMax)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers().set(LumpSumAmountAndDatePage(event8a, 0), validValue).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteEvent8A)

        val view = application.injector.instanceOf[LumpSumAmountAndDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view(form.fill(validValue), waypoints, event8a, 0, stubMin, stubMax)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRouteEvent8A).withFormUrlEncodedBody(lumpSumDetails("1000.00", Some(validDate))*)

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(LumpSumAmountAndDatePage(event8a, 0), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual LumpSumAmountAndDatePage(event8a, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRouteEvent8A).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[LumpSumAmountAndDateView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces()mustEqual view(boundForm, waypoints, event8a, 0, stubMin, stubMax)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }
  }
}

object LumpSumAmountAndDateControllerSpec {
  private val lumpSumValueKey = "lumpSumAmount"
  private val lumpSumDateKey = "lumpSumDate"

  private def lumpSumDetails(
                              lumpSumAmount: String,
                              lumpSumDate: Option[LocalDate]
                            ): Seq[(String, String)] = lumpSumDate match {
    case Some(date) => Seq(
      Tuple2(lumpSumValueKey, lumpSumAmount),
      Tuple2(lumpSumDateKey + ".day", s"${date.getDayOfMonth}"),
      Tuple2(lumpSumDateKey + ".month", s"${date.getMonthValue}"),
      Tuple2(lumpSumDateKey + ".year", s"${date.getYear}")
    )
    case None =>
      Seq(Tuple2(lumpSumValueKey, lumpSumAmount))
  }

  private def validDateCalc(date: LocalDate): LocalDate = {
    date match {
      case _ if date.isBefore(LocalDate.of(date.getYear, 4, 6)) =>
        LocalDate.of(2006, 4, 6)
      case _ =>
        LocalDate.of(date.getYear, 4, 5)
    }
  }

  private val validDate = validDateCalc(LocalDate.of(2023, 1, 1))
}
