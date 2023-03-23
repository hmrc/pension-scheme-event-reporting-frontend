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

package controllers.event8

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event8.LumpSumAmountAndDateFormProvider
import models.UserAnswers
import models.enumeration.EventType.Event8
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
  private val stubMax: LocalDate = LocalDate.of(LocalDate.now().getYear, 4, 5) //05-04-2023

  private val formProvider = new LumpSumAmountAndDateFormProvider()
  private val form = formProvider(stubMin, stubMax)
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val eventType = Event8

  private def getRoute: String = routes.LumpSumAmountAndDateController.onPageLoad(waypoints, 0).url

  private def postRoute: String = routes.LumpSumAmountAndDateController.onSubmit(waypoints, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validValue = LumpSumDetails(1000.00, LocalDate.of(2022, 7, 12))

  import LumpSumAmountAndDateControllerSpec._

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "LumpSumAmountAndDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LumpSumAmountAndDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, 0)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers().set(LumpSumAmountAndDatePage(eventType, 0), validValue).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[LumpSumAmountAndDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validValue), waypoints, 0)(request, messages(application)).toString
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
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(lumpSumDetails("1000.00", Some(validDate)): _*)

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(LumpSumAmountAndDatePage(eventType, 0), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual LumpSumAmountAndDatePage(eventType, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
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

        val view = application.injector.instanceOf[LumpSumAmountAndDateView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, 0)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
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

  private val validDate = validDateCalc(LocalDate.now())
}
