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

package controllers.event2

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event2.AmountPaidFormProvider
import models.{TaxYear, UserAnswers}
import models.common.MembersDetails
import models.enumeration.EventType.Event2
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.common.MembersDetailsPage
import pages.{EmptyWaypoints, TaxYearPage}
import pages.event2.AmountPaidPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.Event2MemberPageNumbers
import views.html.event2.AmountPaidView

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class AmountPaidControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new AmountPaidFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.AmountPaidController.onPageLoad(waypoints, index = 0).url
  private def postRoute: String = routes.AmountPaidController.onSubmit(waypoints, index = 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val correctUserAnswers = UserAnswers().set(TaxYearPage, TaxYear("2022")).get
    .set(MembersDetailsPage(Event2, 0, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY), MembersDetails("Joe", "Bloggs", "ABC1234")).get

  private val validValue: BigDecimal = 33.90

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "AmountPaid Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(correctUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AmountPaidView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view(form, waypoints, 0, "Joe Bloggs")(request, messages(application)).toString
      }

      Await.result(application.stop(), 10.seconds)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = correctUserAnswers.set(AmountPaidPage(index = 0, Event2), validValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[AmountPaidView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view(form.fill(validValue), waypoints, 0, "Joe Bloggs")(request, messages(application)).toString
      }

      Await.result(application.stop(), 10.seconds)
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(correctUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "33.00"))

        val result = route(application, request).value
        val updatedAnswers = correctUserAnswers.set(AmountPaidPage(index = 0, Event2), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AmountPaidPage(index = 0, Event2).navigate(waypoints, correctUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any(), any())
      }

      Await.result(application.stop(), 10.seconds)
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(correctUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ""))

        val view = application.injector.instanceOf[AmountPaidView]
        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces()mustEqual view(boundForm, waypoints, 0, "Joe Bloggs")(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }

      Await.result(application.stop(), 10.seconds)
    }
  }
}
