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

package controllers

import base.SpecBase
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import forms.WantToSubmitFormProvider
import models.enumeration.EventType
import models.enumeration.VersionStatus.Compiled
import models.{EventSummary, TaxYear, UserAnswers, VersionInfo}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, EventSummaryPage, TaxYearPage, VersionInfoPage, WantToSubmitPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.DateHelper
import views.html.WantToSubmitView

import java.time.LocalDate
import scala.concurrent.Future

class WantToSubmitControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new WantToSubmitFormProvider()
  private val form = formProvider()

  private def getRoute: String = routes.WantToSubmitController.onPageLoad(waypoints).url
  private def postRoute: String = routes.WantToSubmitController.onSubmit(waypoints).url

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockEventReportingConnector = mock[EventReportingConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[EventReportingConnector].toInstance(mockEventReportingConnector)
  )

  private val ua = emptyUserAnswers
    .setOrException(EventSummaryPage, true)
    .setOrException(TaxYearPage, TaxYear("2023"))
    .setOrException(VersionInfoPage, VersionInfo(1, Compiled))

  private val uaPreviousTaxYear = emptyUserAnswers
    .setOrException(EventSummaryPage, true)
    .setOrException(TaxYearPage, TaxYear("2022"))
    .setOrException(VersionInfoPage, VersionInfo(1, Compiled))


  private val seqOfEvents = Seq(EventSummary(EventType.Event1, 1, None), EventSummary(EventType.Event18, 1, None))
  private val seqOfEventsWithWindUp = Seq(EventSummary(EventType.Event1, 1, None), EventSummary(EventType.Event18, 1, None), EventSummary(EventType.WindUp, 1, None))
  private val seqOfEventsWithEvent20A = Seq(EventSummary(EventType.Event1, 1, None), EventSummary(EventType.Event18, 1, None), EventSummary(EventType.Event20A, 1, None))
  override def beforeEach(): Unit = {
    super.beforeEach()
    DateHelper.setDate(Some(LocalDate.of(2023, 6, 1)))
    reset(mockUserAnswersCacheConnector)
    reset(mockEventReportingConnector)
  }

  "WantToSubmit Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WantToSubmitView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers().set(WantToSubmitPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[WantToSubmitView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
      }
    }

    "must return bad request when invalid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[WantToSubmitView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never).save(any(), any())(any(), any())
      }
    }

    "must save the answer and redirect to declaration page on submit (when selecting YES) with correct year and Wind Up for a PSA" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockEventReportingConnector.getEventReportSummary(any(), ArgumentMatchers.eq("2023-04-06"), ArgumentMatchers.eq(1))(any())).thenReturn(
        Future.successful(seqOfEventsWithWindUp))

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val userAnswerUpdated = UserAnswers().setOrException(WantToSubmitPage, true)

        status(result) mustEqual SEE_OTHER
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any())
        redirectLocation(result).value mustEqual WantToSubmitPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).url
      }
    }

    "must save the answer and redirect to declaration page on submit (when selecting YES) with correct year and event20A for a PSA" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockEventReportingConnector.getEventReportSummary(any(), ArgumentMatchers.eq("2023-04-06"), ArgumentMatchers.eq(1))(any())).thenReturn(
        Future.successful(seqOfEventsWithEvent20A))

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val userAnswerUpdated = UserAnswers().setOrException(WantToSubmitPage, true)

        status(result) mustEqual SEE_OTHER
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any())
        redirectLocation(result).value mustEqual WantToSubmitPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).url
      }
    }

    "must redirect to cannot submit page on submit (when selecting YES) when current tax year and no wind up for a PSA" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockEventReportingConnector.getEventReportSummary(any(), ArgumentMatchers.eq("2023-04-06"), ArgumentMatchers.eq(1))(any())).thenReturn(
        Future.successful(seqOfEvents))

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any())
        redirectLocation(result).value mustEqual routes.CannotSubmitController.onPageLoad(waypoints).url
      }
    }

    "must save the answer and redirect to event selection page on submit (when selecting YES) for a PSP" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockEventReportingConnector.getEventReportSummary(any(), ArgumentMatchers.eq("2022-04-06"), ArgumentMatchers.eq(1))(any())).thenReturn(
        Future.successful(seqOfEvents))

      val application =
        applicationBuilderForPSP(userAnswers = Some(uaPreviousTaxYear), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any())
        redirectLocation(result).value mustEqual routes.DeclarationPspController.onPageLoad(waypoints).url
      }
    }

    "must save the answer and redirect to next page on submit (when selecting NO)" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockEventReportingConnector.getEventReportSummary(any(), ArgumentMatchers.eq("2023-04-06"), ArgumentMatchers.eq(1))(any())).thenReturn(
        Future.successful(seqOfEvents))

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value
        UserAnswers().setOrException(WantToSubmitPage, false)

        status(result) mustEqual SEE_OTHER
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any())
        redirectLocation(result).value mustEqual request.returnUrl
      }
    }
  }
}
