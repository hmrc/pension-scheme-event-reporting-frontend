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

package controllers.event1

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event1.PaymentValueAndDateFormProvider
import models.event1.PaymentDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.event1.PaymentValueAndDatePage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event1.PaymentValueAndDateView

import java.time.LocalDate
import scala.concurrent.Future

class PaymentValueAndDateControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new PaymentValueAndDateFormProvider()
  private val taxYear = 2022
  private val form = formProvider(taxYear)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.PaymentValueAndDateController.onPageLoad(waypoints, 0).url

  private def postRoute: String = routes.PaymentValueAndDateController.onSubmit(waypoints, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validDate = LocalDate.of(taxYear + 1, 1, 1)
  private val validValue = PaymentDetails(1000.00, validDate)

  import PaymentValueAndDateControllerSpec._

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "PaymentValueAndDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PaymentValueAndDateView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view(form, waypoints, 0, taxYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersWithTaxYear.set(PaymentValueAndDatePage(0), validValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[PaymentValueAndDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view(form.fill(validValue), waypoints, 0, taxYear)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(paymentDetails("1000.00", Some(validDate))*)

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswersWithTaxYear.set(PaymentValueAndDatePage(0), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual PaymentValueAndDatePage(0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ""))

        val view = application.injector.instanceOf[PaymentValueAndDateView]
        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces()mustEqual view(boundForm, waypoints, 0, 2022)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }
  }
}

object PaymentValueAndDateControllerSpec {
  private val paymentValueKey = "paymentValue"
  private val paymentDateKey = "paymentDate"

  private def paymentDetails(
                              paymentValue: String,
                              paymentDate: Option[LocalDate]
                            ): Seq[(String, String)] = paymentDate match {
    case Some(date) => Seq(
      Tuple2(paymentValueKey, paymentValue),
      Tuple2(paymentDateKey + ".day", s"${date.getDayOfMonth}"),
      Tuple2(paymentDateKey + ".month", s"${date.getMonthValue}"),
      Tuple2(paymentDateKey + ".year", s"${date.getYear}")
    )
    case None =>
      Seq(Tuple2(paymentValueKey, paymentValue))
  }
}

