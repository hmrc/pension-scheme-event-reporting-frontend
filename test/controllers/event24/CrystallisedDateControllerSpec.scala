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

package controllers.event24

import base.SpecBase
import connectors.UserAnswersCacheConnector
import controllers.event24.CrystallisedDateControllerSpec.crystallisedDate
import forms.event24.CrystallisedDateFormProvider
import models.event24.CrystallisedDate
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import pages.event24.CrystallisedDatePage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event24.CrystallisedDateView

import java.time.LocalDate
import scala.concurrent.Future

class CrystallisedDateControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new CrystallisedDateFormProvider()
  private val stubMin: LocalDate = LocalDate.of(2022, 4, 6)
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5)
  private val form = formProvider(stubMin, stubMax)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.CrystallisedDateController.onPageLoad(waypoints, 0).url

  private def postRoute: String = routes.CrystallisedDateController.onSubmit(waypoints, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validAnswer = CrystallisedDate(LocalDate.of(2023, 2, 12))


  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "CrystallisedDateController" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswersWithTaxYear

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CrystallisedDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, 0)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswersWithTaxYear.set(CrystallisedDatePage(0), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[CrystallisedDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), waypoints, 0)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val userAnswers = emptyUserAnswersWithTaxYear

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(
            crystallisedDate(validAnswer.date): _*
          )

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(CrystallisedDatePage(0), validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CrystallisedDatePage(0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }
    "must return a bad request when the user enters a date outside of the valid range" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val userAnswers = emptyUserAnswersWithTaxYear

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), extraModules)
          .build()

      val invalidAnswer = CrystallisedDate(LocalDate.of(2006, 2, 12))

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(
            crystallisedDate(invalidAnswer.date): _*
          )

        val result = route(application, request).value

        val view = application.injector.instanceOf[CrystallisedDateView]
        val boundForm = form.bind(Map("value" -> invalidAnswer.toString))

        status(result) mustEqual BAD_REQUEST
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val userAnswers = emptyUserAnswersWithTaxYear

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[CrystallisedDateView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, 0)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}

object CrystallisedDateControllerSpec {
  private val crystallisedDateKey = "crystallisedDate"

  private def crystallisedDate(
                                date: LocalDate
                              ): Seq[(String, String)] = {
    val day = Tuple2(crystallisedDateKey + ".day", s"${date.getDayOfMonth}")
    val month = Tuple2(crystallisedDateKey + ".month", s"${date.getMonthValue}")
    val year = Tuple2(crystallisedDateKey + ".year", s"${date.getYear}")
    Seq(day, month, year)
  }

}
