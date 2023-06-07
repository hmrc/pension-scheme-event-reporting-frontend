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

package controllers.event20

import base.SpecBase
import connectors.UserAnswersCacheConnector
import controllers.event20.BecameDateControllerSpec.becameDate
import forms.event20.BecameDateFormProvider
import models.event20.{Event20Date, WhatChange}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.event20.{BecameDatePage, WhatChangePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event20.BecameDateView

import java.time.LocalDate
import scala.concurrent.Future

class BecameDateControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new BecameDateFormProvider()
  private val stubMin: LocalDate = LocalDate.of(2022, 4, 6)
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5)
  private val form = formProvider(stubMin, stubMax)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.BecameDateController.onPageLoad(waypoints).url

  private def postRoute: String = routes.BecameDateController.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validAnswer = Event20Date(LocalDate.of(2022, 7, 12))

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "BecameDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(WhatChangePage, WhatChange.BecameOccupationalScheme)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BecameDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(WhatChangePage, WhatChange.BecameOccupationalScheme)
        .set(BecameDatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[BecameDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), waypoints)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(WhatChangePage, WhatChange.BecameOccupationalScheme)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(becameDate(validAnswer.date): _*)

        val result = route(application, request).value
        val updatedAnswers = userAnswers.set(BecameDatePage, validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual BecameDatePage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(WhatChangePage, WhatChange.BecameOccupationalScheme)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("becameDate", "invalid"))

        val view = application.injector.instanceOf[BecameDateView]
        val boundForm = form.bind(Map("becameDate" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}

object BecameDateControllerSpec {
  private val becameDateKey = "becameDate"

  private def becameDate(
                                becameDate: LocalDate
                              ): Seq[(String, String)] = {
    val day = Tuple2(becameDateKey + ".day", s"${becameDate.getDayOfMonth}")
    val month = Tuple2(becameDateKey + ".month", s"${becameDate.getMonthValue}")
    val year = Tuple2(becameDateKey + ".year", s"${becameDate.getYear}")
    Seq(day, month, year)
  }

}
