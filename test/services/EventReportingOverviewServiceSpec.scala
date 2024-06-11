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
import data.SampleData.convertScalaFuture
import models.enumeration.EventType.Event1
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.http.HeaderCarrier
import models.{EROverview, EROverviewVersion, EventDataIdentifier, TaxYear, UserAnswers, VersionInfo}
import models.enumeration.JourneyStartType.InProgress
import models.enumeration.VersionStatus.{Compiled, NotStarted}
import org.apache.pekko.actor.ActorSystem
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{EventReportingOverviewPage, EventReportingTileLinksPage, TaxYearPage, VersionInfoPage}

import java.time.LocalDate

class EventReportingOverviewServiceSpec extends SpecBase with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)

  private val pstr = "pstr"
  private val taxYear = "2022"
  private val currentTaxYear = TaxYear(taxYear)
  private val seqEROverview = Seq(EROverview(
    LocalDate.now(), LocalDate.now().plusYears(1),
    currentTaxYear, tpssReportPresent = false,
    Some(EROverviewVersion(1, submittedVersionAvailable = true, compiledVersionAvailable = false))))
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

  override def beforeEach(): Unit = {
    reset(mockEventReportingConnector)
    reset(mockUserAnswersCacheConnector)
    reset(mockAppConfig)

    when(mockAppConfig.compileDelayInSeconds).thenReturn(0)
    when(mockUserAnswersCacheConnector.get(any()) (any(), any()))
      .thenReturn(Future.successful((Some(ua))))

    when(mockUserAnswersCacheConnector.get(any()) (any(), any()) )
      .thenReturn(Future.successful(None))

    when(mockUserAnswersCacheConnector.get(any(), any()) (any(), any()))
      .thenReturn(Future.successful((Some(ua))))

    when(mockUserAnswersCacheConnector.save(any(), any()) (any(), any()))
      .thenReturn(Future.successful(()))
    when(mockUserAnswersCacheConnector.removeAll(any())(any(), any()))
      .thenReturn(Future.successful(()))
    when(mockEventReportingConnector.getOverview(any(), any(), any(), any())(any()))
      .thenReturn(Future.successful((Seq.empty)))
  }

    "getInProgressYearAndUrl" - {

      "return the correct in progress years and URLs" in {
        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        val edi = EventDataIdentifier(Event1, taxYear, "1")
        when(mockUserAnswersCacheConnector.get(any())(any(), any()))
          .thenReturn(Future.successful(Some((ua))))

        whenReady(eventReportingService.getInProgressYearAndUrl(ua, pstr)) { _ =>
          verify(mockUserAnswersCacheConnector, times(1))
            .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any())
          val actualUAAfterSave = captor.getValue
          actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(2, Compiled))
        }
      }
    }

  "getPastYearsAndUrl" - {

    "return the correct past years and URLs" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val edi = EventDataIdentifier(Event1, taxYear, "1")
      when(mockUserAnswersCacheConnector.get(any())(any(), any()))
        .thenReturn(Future.successful(Some((ua))))

      eventReportingService.getPastYearsAndUrl(ua, pstr).futureValue.size mustBe 7

    }
  }
}