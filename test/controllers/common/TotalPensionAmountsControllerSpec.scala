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

package controllers.common

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.common.TotalPensionAmountsFormProvider
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
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
  private val seqOfEvents = Seq(Event22, Event23)
  private val formProvider = new TotalPensionAmountsFormProvider()
  private val form = formProvider()
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val validValue = BigDecimal(1000.00)

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private def getRoute(eventType: EventType): String = routes.TotalPensionAmountsController.onPageLoad(waypoints, eventType, 0).url

  private def postRoute(eventType: EventType): String = routes.TotalPensionAmountsController.onSubmit(waypoints, eventType, 0).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "TotalPensionAmounts Controller" - {
    for (event <- seqOfEvents) {
      testReturnOkAndCorrectView(event)
      testPopulateCorrectViewOnGetWhenPrevAnswered(event)
      testSaveAnswerAndRedirectWhenValid(event)
      testBadRequestForInvalidDataSubmission(event)
    }
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))
        val result = route(application, request).value
        val view = application.injector.instanceOf[TotalPensionAmountsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(form, waypoints, eventType, StringUtils.EMPTY, index = 0, request, messages(application)).toString
      }
    }
  }

  private def testPopulateCorrectViewOnGetWhenPrevAnswered(eventType: EventType): Unit = {
    s"must populate the view correctly on a GET when the question has previously been answered  for Event $eventType" in {
      val userAnswers = emptyUserAnswersWithTaxYear.set(TotalPensionAmountsPage(eventType, 0), validValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))
        val view = application.injector.instanceOf[TotalPensionAmountsView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(
          form.fill(validValue),
          waypoints,
          eventType,
          StringUtils.EMPTY,
          index = 0,
          request,
          messages(application)).toString
      }
    }
  }

  private def testSaveAnswerAndRedirectWhenValid(eventType: EventType): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for Event $eventType" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any())).thenReturn(Future.successful(()))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()
      running(application) {
        val request = FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", "33.00"))
        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(TotalPensionAmountsPage(eventType, 0), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TotalPensionAmountsPage(eventType, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }
  }

  private def testBadRequestForInvalidDataSubmission(eventType: EventType): Unit = {
    s"must return bad request when invalid data is submitted for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", ""))
        val view = application.injector.instanceOf[TotalPensionAmountsView]
        val boundForm = form.bind(Map("value" -> ""))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view.render(boundForm, waypoints, eventType, StringUtils.EMPTY, index = 0, request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }

}
