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

package services

import base.SpecBase
import config.FrontendAppConfig
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.enumeration.VersionStatus.Compiled
import models.requests.DataRequest
import models.{EROverview, EROverviewVersion, LoggedInUser, TaxYear, UserAnswers, VersionInfo}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{EventReportingOverviewPage, VersionInfoPage}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EventReportingOverviewServiceSpec extends SpecBase with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)

  private val pstr = "pstr"
  val versionDetails = EROverviewVersion(numberOfVersions = 1, compiledVersionAvailable = true, submittedVersionAvailable = true)
  val erOverview = EROverview(
    LocalDate.of(2021, 4, 6),
    LocalDate.of(2022, 4, 5),
    TaxYear(2022.toString), true,
    Some(versionDetails))

  val eventReportingOverviewPageData = Seq(erOverview)
  private val mockEventReportingConnector = mock[EventReportingConnector]
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockAppConfig = mock[FrontendAppConfig]

  val application = applicationBuilder()

  private val eventReportingService = new EventReportingOverviewService( mockUserAnswersCacheConnector, mockAppConfig)
  private implicit val dataRequest: DataRequest[AnyContent] =
    DataRequest("Pstr123", "SchemeABC", "returnUrl", FakeRequest(GET, "/"), LoggedInUser("user", Administrator, "psaId"), UserAnswers(), "S2400000041")

  override def beforeEach(): Unit = {
    reset(mockEventReportingConnector)
    reset(mockUserAnswersCacheConnector)
    reset(mockAppConfig)

    when(mockAppConfig.compileDelayInSeconds).thenReturn(0)
    when(mockUserAnswersCacheConnector.getBySrn(any(), any()) (any(), any()))
      .thenReturn(Future.successful((Some(ua))))

    when(mockUserAnswersCacheConnector.getBySrn(any(), any()) (any(), any()) )
      .thenReturn(Future.successful(None))

    when(mockUserAnswersCacheConnector.getByEventType(any(), any()) (any(), any(), any()))
      .thenReturn(Future.successful((Some(ua))))

    when(mockUserAnswersCacheConnector.save(any(), any()) (any(), any(), any()))
      .thenReturn(Future.successful(()))
    when(mockUserAnswersCacheConnector.removeAll(any())(any(), any(), any()))
      .thenReturn(Future.successful(()))
    when(mockEventReportingConnector.getOverview(any(), any(), any(), any())(any(), any()))
      .thenReturn(Future.successful((Seq.empty)))
  }

  "getInProgressYearAndUrl" - {

    "return the correct in progress years and URLs" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockUserAnswersCacheConnector.getBySrn(any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(ua)))

      whenReady(eventReportingService.getInProgressYearAndUrl(ua, pstr)) { _ =>
        verify(mockUserAnswersCacheConnector, times(1))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any(), any())
        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(2, Compiled))
      }
    }

    "use InProgress journey URL when single compiled year also has a submitted version" in {
      val compiledAndSubmitted = EROverview(
        LocalDate.of(2020, 4, 6),
        LocalDate.of(2021, 4, 5),
        TaxYear("2020"), tpssReportPresent = false,
        Some(EROverviewVersion(numberOfVersions = 2, compiledVersionAvailable = true, submittedVersionAvailable = true))
      )
      val uaWithOverview = ua.setOrException(EventReportingOverviewPage, Seq(compiledAndSubmitted), nonEventTypeData = true)
      when(mockUserAnswersCacheConnector.getBySrn(any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(uaWithOverview)))

      val result = eventReportingService.getInProgressYearAndUrl(uaWithOverview, pstr).futureValue
      result must have size 1
      result.head._1 mustBe "6 April 2020 to 5 April 2021"
      result.head._2 must include("InProgress")
      result.head._2 must not include "PastEventTypes"
    }

    "return all compiled years when multiple years have compiledVersionAvailable" in {
      val compiled2020 = EROverview(
        LocalDate.of(2020, 4, 6), LocalDate.of(2021, 4, 5), TaxYear("2020"), tpssReportPresent = false,
        Some(EROverviewVersion(numberOfVersions = 2, compiledVersionAvailable = true, submittedVersionAvailable = true))
      )
      val compiled2024 = EROverview(
        LocalDate.of(2024, 4, 6), LocalDate.of(2025, 4, 5), TaxYear("2024"), tpssReportPresent = false,
        Some(EROverviewVersion(numberOfVersions = 2, compiledVersionAvailable = true, submittedVersionAvailable = true))
      )
      val uaWithOverview = ua.setOrException(EventReportingOverviewPage, Seq(compiled2020, compiled2024), nonEventTypeData = true)
      when(mockUserAnswersCacheConnector.getBySrn(any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(uaWithOverview)))

      val result = eventReportingService.getInProgressYearAndUrl(uaWithOverview, pstr).futureValue
      result.map(_._1) must contain allOf ("6 April 2020 to 5 April 2021", "6 April 2024 to 5 April 2025")
    }
  }

  "getPastYearsAndUrl" - {

    "return the correct past years and URLs" in {
      when(mockUserAnswersCacheConnector.getBySrn(any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(ua)))

      eventReportingService.getPastYearsAndUrl(ua, pstr).futureValue.size mustBe 7
    }

    "include years where tpssReportPresent is true even when versionDetails is absent" in {
      val tpssYear = EROverview(
        LocalDate.of(2024, 4, 6), LocalDate.of(2025, 4, 5),
        TaxYear("2024"), tpssReportPresent = true, versionDetails = None
      )
      val uaWithOverview = ua.setOrException(EventReportingOverviewPage, Seq(tpssYear), nonEventTypeData = true)
      when(mockUserAnswersCacheConnector.getBySrn(any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(uaWithOverview)))

      val result = eventReportingService.getPastYearsAndUrl(uaWithOverview, pstr).futureValue
      result.map(_._1) must contain("6 April 2024 to 5 April 2025")
    }

    "include a year with submittedVersionAvailable in past years" in {
      val submittedYear = EROverview(
        LocalDate.of(2024, 4, 6), LocalDate.of(2025, 4, 5),
        TaxYear("2024"), tpssReportPresent = false,
        Some(EROverviewVersion(numberOfVersions = 1, compiledVersionAvailable = false, submittedVersionAvailable = true))
      )
      val uaWithOverview = ua.setOrException(EventReportingOverviewPage, Seq(submittedYear), nonEventTypeData = true)
      when(mockUserAnswersCacheConnector.getBySrn(any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(uaWithOverview)))

      val result = eventReportingService.getPastYearsAndUrl(uaWithOverview, pstr).futureValue
      result.map(_._1) must contain("6 April 2024 to 5 April 2025")
    }
  }
}