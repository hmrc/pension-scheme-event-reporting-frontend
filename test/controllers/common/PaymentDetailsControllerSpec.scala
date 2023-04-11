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
import controllers.common.PaymentDetailsControllerSpec.{paymentDetails, validDate}
import forms.common.PaymentDetailsFormProvider
import models.UserAnswers
import models.common.PaymentDetails
import models.enumeration.EventType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.common.PaymentDetailsPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.common.PaymentDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class PaymentDetailsControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val stubMin: LocalDate = LocalDate.of(2006, 4, 6) //06-04-2006
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5) //05-04-2023

  private val formProvider = new PaymentDetailsFormProvider()
  private val form = formProvider(stubMin, stubMax)
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val event3 = EventType.Event3
  private val event4 = EventType.Event4
  private val event5 = EventType.Event5

  private def getRoute(eventType: EventType): String = routes.PaymentDetailsController.onPageLoad(waypoints, eventType, 0).url

  private def postRoute(eventType: EventType): String = routes.PaymentDetailsController.onSubmit(waypoints, eventType, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validValue = PaymentDetails(500.00, LocalDate.of(2022, 7, 12))

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "PaymentDetails Controller" - {
    testSuite(event3)
    testSuite(event4)
    testSuite(event5)
  }

  private def testSuite(eventType: EventType): Unit = {
    testReturnOkAndCorrectView(eventType)
    testPopulateCorrectViewOnGetWhenPrevAnswered(eventType)
    testSaveAnswerAndRedirectWhenValid(eventType)
    testBadRequestForInvalidDataSubmission(eventType)
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET for Event $eventType" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))

        val result = route(application, request).value

        val view = application.injector.instanceOf[PaymentDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, eventType, 0)(request, messages(application)).toString
      }
    }
  }

  private def testPopulateCorrectViewOnGetWhenPrevAnswered(eventType: EventType): Unit = {
    s"must populate the view correctly on a GET when the question has previously been answered  for Event $eventType" in {
      val userAnswers = UserAnswers().set(PaymentDetailsPage(eventType, 0), validValue).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))

        val view = application.injector.instanceOf[PaymentDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validValue), waypoints, eventType, 0)(request, messages(application)).toString
      }
    }
  }

  private def testSaveAnswerAndRedirectWhenValid(eventType: EventType): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for Event $eventType" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(paymentDetails("1000.00", Some(validDate)): _*)

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(PaymentDetailsPage(eventType, 0), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual PaymentDetailsPage(eventType, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }
  }

  private def testBadRequestForInvalidDataSubmission(eventType: EventType): Unit = {
    s"must return bad request when invalid data is submitted for Event $eventType" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[PaymentDetailsView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, eventType, 0)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}

object PaymentDetailsControllerSpec {
  private val amountPaidKey = "amountPaid"
  private val eventDateKey = "eventDate"

  private def paymentDetails(
                              amountPaid: String,
                              eventDate: Option[LocalDate]
                            ): Seq[(String, String)] = eventDate match {
    case Some(date) => Seq(
      Tuple2(amountPaidKey, amountPaid),
      Tuple2(eventDateKey + ".day", s"${date.getDayOfMonth}"),
      Tuple2(eventDateKey + ".month", s"${date.getMonthValue}"),
      Tuple2(eventDateKey + ".year", s"${date.getYear}")
    )
    case None =>
      Seq(Tuple2(amountPaidKey, amountPaid))
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
