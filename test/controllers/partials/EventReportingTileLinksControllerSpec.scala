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

package controllers.partials

import base.SpecBase
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.{EROverview, EROverviewVersion}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, EventReportingOverviewPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class EventReportingTileLinksControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val mockConnector = mock[EventReportingConnector]
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[EventReportingConnector].to(mockConnector),
    bind[UserAnswersCacheConnector].to(mockUserAnswersCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "Event Reporting Tile Controller" - {
    "must redirect to tax year page for a GET when 2 compiled versions available" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      val overview1 = EROverview(
        LocalDate.of(2021, 4, 6),
        LocalDate.of(2022, 4, 5),
        tpssReportPresent = false,
        Some(EROverviewVersion(
          3,
          submittedVersionAvailable = false,
          compiledVersionAvailable = true)))

      val overview2 = EROverview(
        LocalDate.of(2022, 4, 6),
        LocalDate.of(2023, 4, 5),
        tpssReportPresent = false,
        Some(EROverviewVersion(
          2,
          submittedVersionAvailable = false,
          compiledVersionAvailable = true)))

      val seqEROverview: Seq[EROverview] = Seq(
        overview1, overview2
      )

      val ua = emptyUserAnswersWithTaxYear.setOrException(EventReportingOverviewPage, seqEROverview)
      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.partials.routes.EventReportingTileLinksController.onClickCompiled.url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaxYearController.onPageLoad(EmptyWaypoints).url
      }
    }

    "must redirect to summary page for a GET when 1 compiled version available but >1 overview item" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      val overview1 = EROverview(
        LocalDate.of(2021, 4, 6),
        LocalDate.of(2022, 4, 5),
        tpssReportPresent = false,
        Some(EROverviewVersion(
          3,
          submittedVersionAvailable = false,
          compiledVersionAvailable = true)))

      val overview2 = EROverview(
        LocalDate.of(2022, 4, 6),
        LocalDate.of(2023, 4, 5),
        tpssReportPresent = false,
        Some(EROverviewVersion(
          2,
          submittedVersionAvailable = false,
          compiledVersionAvailable = false)))

      val seqEROverview: Seq[EROverview] = Seq(
        overview1, overview2
      )

      val ua = emptyUserAnswersWithTaxYear.setOrException(EventReportingOverviewPage, seqEROverview)
      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.partials.routes.EventReportingTileLinksController.onClickCompiled.url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url
      }
    }

    "must redirect to summary page for a GET when 1 compiled version available and 1 overview item" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      val overview1 = EROverview(
        LocalDate.of(2021, 4, 6),
        LocalDate.of(2022, 4, 5),
        tpssReportPresent = false,
        Some(EROverviewVersion(
          3,
          submittedVersionAvailable = false,
          compiledVersionAvailable = true)))

      val seqEROverview: Seq[EROverview] = Seq(overview1)

      val ua = emptyUserAnswersWithTaxYear.setOrException(EventReportingOverviewPage, seqEROverview)
      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.partials.routes.EventReportingTileLinksController.onClickCompiled.url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url
      }
    }


  }
}

