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
import models.common.ChooseTaxYear
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, common}
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Writes
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.DateHelper
import views.html.common.ChooseTaxYearView

import java.time.{LocalDate, Month}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class ChooseTaxYearControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val seqOfEvents = Seq(Event22, Event23)
  private val maxTaxYear = 2022
  private val maxTaxYearForWrites = 2021
  private val chooseTaxYearFormProvider = new ChooseTaxYearFormProvider()
  private val writesTaxYear: Writes[ChooseTaxYear] = ChooseTaxYear.writes(ChooseTaxYear.enumerable(taxYearMax = maxTaxYearForWrites))
  private val waypoints = EmptyWaypoints
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val dateYear = 2023
  private val date = LocalDate.of(dateYear, Month.JUNE, 1)

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private def form(eventType: EventType): Form[ChooseTaxYear] = chooseTaxYearFormProvider(eventType, maxTaxYear)

  private def getRoute(eventType: EventType): String = routes.ChooseTaxYearController.onPageLoad(waypoints, eventType, index = 0).url

  private def postRoute(eventType: EventType): String = routes.ChooseTaxYearController.onSubmit(waypoints, eventType, index = 0).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    DateHelper.setDate(Some(date))
    reset(mockUserAnswersCacheConnector)
  }

  "ChooseTaxYear Controller" - {
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

        val view = application.injector.instanceOf[ChooseTaxYearView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(form(eventType), waypoints, eventType, index = 0, maxTaxYear, request, messages(application)).toString
      }

      Await.result(application.stop(), 10.seconds)
    }
  }

  private def testPopulateCorrectViewOnGetWhenPrevAnswered(eventType: EventType): Unit = {
    s"must populate the view correctly on a GET when the question has previously been answered  for Event $eventType" in {
      val userAnswers = emptyUserAnswersWithTaxYear.set(common.ChooseTaxYearPage(eventType, 0),
        ChooseTaxYear.values(maxTaxYear).head)(writesTaxYear).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))

        val view = application.injector.instanceOf[ChooseTaxYearView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(form(eventType).fill(
          ChooseTaxYear.values(maxTaxYear).head),
          waypoints,
          eventType,
          index = 0,
          maxTaxYear,
          request = request,
          messages = messages(application)).toString
      }

      Await.result(application.stop(), 10.seconds)
    }
  }

  private def testSaveAnswerAndRedirectWhenValid(eventType: EventType): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for Event $eventType" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any())).thenReturn(Future.successful(()))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", ChooseTaxYear.values(maxTaxYear).head.toString))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(common.ChooseTaxYearPage(eventType, 0), ChooseTaxYear.values(maxTaxYear).head)(writesTaxYear).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual common.ChooseTaxYearPage(eventType, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }

      Await.result(application.stop(), 10.seconds)
    }
  }

  private def testBadRequestForInvalidDataSubmission(eventType: EventType): Unit = {
    s"must return bad request when invalid data is submitted for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[ChooseTaxYearView]
        val boundForm = form(eventType).bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view.render(boundForm, waypoints, eventType, 0, maxTaxYear, request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }

      Await.result(application.stop(), 10.seconds)
    }
  }

}
