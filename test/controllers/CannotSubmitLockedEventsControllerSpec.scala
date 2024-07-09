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

package controllers

import base.SpecBase
import connectors.EventReportingConnector
import models.enumeration.EventType
import models.enumeration.VersionStatus.Compiled
import models.{EROverview, EROverviewVersion, EventSummary, TaxYear, VersionInfo}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, EventReportingOverviewPage, EventSummaryPage, TaxYearPage, VersionInfoPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.CannotSubmitLockedEventsView

import java.time.LocalDate
import scala.concurrent.Future

class CannotSubmitLockedEventsControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach with MockitoSugar {
  private val mockEventReportSummaryConnector = mock[EventReportingConnector]
  private val waypoints = EmptyWaypoints

  val erOverviewSeq = Seq(EROverview(
    LocalDate.of(2022, 4, 6),
    LocalDate.of(2023, 4, 5),
    TaxYear("2022"),
    tpssReportPresent = true,
    Some(EROverviewVersion(
      1,
      submittedVersionAvailable = true,
      compiledVersionAvailable = false
    ))
  ))

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[EventReportingConnector].toInstance(mockEventReportSummaryConnector)
  )

  override protected def beforeEach(): Unit = {
    reset(mockEventReportSummaryConnector)
  }

  "CannotSubmitLockedEventsController Controller" - {

    "must return OK and the correct view for a GET when single event locked" in {

      val ua = emptyUserAnswers
        .setOrException(EventSummaryPage, true)
        .setOrException(TaxYearPage, TaxYear("2022"))
        .setOrException(VersionInfoPage, VersionInfo(1, Compiled))
        .setOrException(EventReportingOverviewPage, erOverviewSeq)

      val seqOfEvents = Seq(EventSummary(EventType.Event1, 1, None), EventSummary(EventType.Event8, 1, Some("TestUser")),
        EventSummary(EventType.Event18, 1, None))
      val expectedSeqOfEvent = Seq(EventSummary(EventType.Event8, 1, Some("TestUser")))

      when(mockEventReportSummaryConnector.getEventReportSummary(any(), ArgumentMatchers.eq("2022-04-06"), ArgumentMatchers.eq(1))(any())).thenReturn(
        Future.successful(seqOfEvents)
      )

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      val view = application.injector.instanceOf[CannotSubmitLockedEventsView]

      running(application) {
        val request = FakeRequest(GET, routes.CannotSubmitLockedEventsController.onPageLoad().url)
        val result = route(application, request).value

        val eventSelectionUrl = routes.EventSelectionController.onPageLoad().url

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(eventSelectionUrl, expectedSeqOfEvent, false)(request, messages(application)).toString
      }
    }


    "must return OK and the correct view for a GET when more than one events locked" in {

      val ua = emptyUserAnswers
        .setOrException(EventSummaryPage, true)
        .setOrException(TaxYearPage, TaxYear("2022"))
        .setOrException(VersionInfoPage, VersionInfo(1, Compiled))
        .setOrException(EventReportingOverviewPage, erOverviewSeq)

      val seqOfEvents = Seq(EventSummary(EventType.Event1, 1, Some("TestUser8")), EventSummary(EventType.Event8, 1, Some("TestUser")),
        EventSummary(EventType.Event18, 1, None))
      val expectedSeqOfEvents = Seq(EventSummary(EventType.Event1, 1, Some("TestUser8")), EventSummary(EventType.Event8, 1, Some("TestUser")))

      when(mockEventReportSummaryConnector.getEventReportSummary(any(), ArgumentMatchers.eq("2022-04-06"), ArgumentMatchers.eq(1))(any())).thenReturn(
        Future.successful(seqOfEvents)
      )

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      val view = application.injector.instanceOf[CannotSubmitLockedEventsView]

      running(application) {
        val request = FakeRequest(GET, routes.CannotSubmitLockedEventsController.onPageLoad().url)
        val result = route(application, request).value

        val eventSelectionUrl = routes.EventSelectionController.onPageLoad().url

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(eventSelectionUrl, expectedSeqOfEvents, true)(request, messages(application)).toString
      }
    }

    "must redirect to WantToSubmitController when no events locked" in {

      val ua = emptyUserAnswers
        .setOrException(EventSummaryPage, true)
        .setOrException(TaxYearPage, TaxYear("2022"))
        .setOrException(VersionInfoPage, VersionInfo(1, Compiled))
        .setOrException(EventReportingOverviewPage, erOverviewSeq)

      val seqOfEvents = Seq(EventSummary(EventType.Event1, 1, None), EventSummary(EventType.Event8, 1, None),
        EventSummary(EventType.Event18, 1, None))

      when(mockEventReportSummaryConnector.getEventReportSummary(any(), ArgumentMatchers.eq("2022-04-06"), ArgumentMatchers.eq(1))(any())).thenReturn(
        Future.successful(seqOfEvents)
      )

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, routes.CannotSubmitLockedEventsController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual  routes.WantToSubmitController.onPageLoad(waypoints).url
      }
    }
  }
}
