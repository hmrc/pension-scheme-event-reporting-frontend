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
import pages.VersionInfoPage
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
          .thenReturn(Future.successful(Some((ua))))

        whenReady(eventReportingService.getInProgressYearAndUrl(ua, pstr)) { _ =>
          verify(mockUserAnswersCacheConnector, times(1))
            .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any(), any())
          val actualUAAfterSave = captor.getValue
          actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(2, Compiled))
        }
      }
    }

  "getPastYearsAndUrl" - {

    "return the correct past years and URLs" in {
      when(mockUserAnswersCacheConnector.getBySrn(any(), any())(any(), any()))
        .thenReturn(Future.successful(Some((ua))))

      eventReportingService.getPastYearsAndUrl(ua, pstr).futureValue.size mustBe 7

    }
  }
}