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

import org.apache.pekko.actor.ActorSystem
import base.SpecBase
import config.FrontendAppConfig
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.enumeration.EventType.Event1
import models.enumeration.VersionStatus.{Compiled, NotStarted, Submitted}
import models.requests.DataRequest
import models.{EROverview, EROverviewVersion, EventDataIdentifier, LoggedInUser, TaxYear, UserAnswers, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.RecoverMethods.recoverToSucceededIf
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{EventReportingOverviewPage, TaxYearPage, VersionInfoPage}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//scalastyle:off magic.number
class CompileServiceSpec extends SpecBase with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val pstr = "pstr"
  private val taxYear = "2022"
  private val currentTaxYear = TaxYear(taxYear)
  private val seqEROverview = Seq(EROverview(
    LocalDate.now(), LocalDate.now().plusYears(1),
    currentTaxYear, tpssReportPresent = false,
    Some(EROverviewVersion(1, submittedVersionAvailable = true, compiledVersionAvailable = false))))
  val oldUserAnswers = UserAnswers().set(TaxYearPage, TaxYear("2022")).success.value
  val newUserAnswers = UserAnswers().set(TaxYearPage, TaxYear("2023")).success.value

  private val mockEventReportingConnector = mock[EventReportingConnector]
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockAppConfig = mock[FrontendAppConfig]

  val application = applicationBuilder()

  private val compileService = new CompileService(mockEventReportingConnector, mockUserAnswersCacheConnector, mockAppConfig, application.injector().instanceOf[ActorSystem])

  private implicit val dataRequest: DataRequest[AnyContent] =
    DataRequest("Pstr123", "SchemeABC", "returnUrl", FakeRequest(GET, "/"), LoggedInUser("user", Administrator, "psaId"), UserAnswers(), "S2400000041")

  override def beforeEach(): Unit = {
    reset(mockEventReportingConnector)
    reset(mockUserAnswersCacheConnector)
    reset(mockAppConfig)
    when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
      .thenReturn(Future.successful((): Unit))
    when(mockAppConfig.compileDelayInSeconds).thenReturn(0)
  }

  "compileEvent" - {
    "must call connector with correct tax year and version when not started" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val edi = EventDataIdentifier(Event1, taxYear, "1")
      when(mockEventReportingConnector.compileEvent(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi), any(), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))

      when(mockUserAnswersCacheConnector.isDataModified(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi.eventType))(any(), any(), any()))
        .thenReturn(Future.successful(Some(true)))

      when(mockUserAnswersCacheConnector.getByEventType(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi.eventType))(any(), any(), any()))
        .thenReturn(Future.successful(Some(newUserAnswers)))

      when(mockUserAnswersCacheConnector.getByEventType(ArgumentMatchers.eq(pstr + "_original_cache"),
        ArgumentMatchers.eq(edi.eventType))(any(), any(), any()))
        .thenReturn(Future.successful(Some(oldUserAnswers)))

      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, NotStarted), nonEventTypeData = true)
      whenReady(compileService.compileEvent(Event1, pstr, ua)) { _ =>
        verify(mockUserAnswersCacheConnector, times(1))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any(), any())
        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(1, Compiled))
      }
    }

    "must call connector with correct tax year and version when in progress (being compiled)" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val edi = EventDataIdentifier(Event1, taxYear, "2")
      when(mockEventReportingConnector.compileEvent(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi), any(), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))
      when(mockUserAnswersCacheConnector.getByEventType(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi.eventType))(any(), any(), any()))
        .thenReturn(Future.successful(Some(newUserAnswers)))
      when(mockUserAnswersCacheConnector.isDataModified(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi.eventType))(any(), any(), any()))
        .thenReturn(Future.successful(Some(true)))
      when(mockUserAnswersCacheConnector.getByEventType(ArgumentMatchers.eq(pstr + "_original_cache"), ArgumentMatchers.eq(edi.eventType))(any(), any(), any()))
        .thenReturn(Future.successful(Some(oldUserAnswers)))

      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      whenReady(compileService.compileEvent(Event1, pstr, ua)) { _ =>
        verify(mockUserAnswersCacheConnector, times(1))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any(), any())
        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(2, Compiled))
      }
    }

    "must call connector with correct tax year and version when submitted status + version 1: increase version to 2, amend status + update overview" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val edi = EventDataIdentifier(Event1, taxYear, "2")
      when(mockEventReportingConnector.compileEvent(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi), any(), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))
      when(mockUserAnswersCacheConnector
        .changeVersion(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq("1"), ArgumentMatchers.eq("2"))(any(), any(), any()))
        .thenReturn(Future.successful((): Unit))
      when(mockUserAnswersCacheConnector.getByEventType(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi.eventType))(any(), any(), any()))
        .thenReturn(Future.successful(Some(newUserAnswers)))
      when(mockUserAnswersCacheConnector.isDataModified(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi.eventType))(any(), any(), any()))
        .thenReturn(Future.successful(Some(true)))
      when(mockUserAnswersCacheConnector.getByEventType(ArgumentMatchers.eq(pstr + "_original_cache"), ArgumentMatchers.eq(edi.eventType))(any(), any(), any()))
        .thenReturn(Future.successful(Some(oldUserAnswers)))

      val ua = emptyUserAnswersWithTaxYear
        .setOrException(TaxYearPage, currentTaxYear, nonEventTypeData = true)
        .setOrException(EventReportingOverviewPage, seqEROverview, nonEventTypeData = true)
        .setOrException(VersionInfoPage, VersionInfo(1, Submitted), nonEventTypeData = true)

      whenReady(compileService.compileEvent(Event1, pstr, ua)) { _ =>
        verify(mockUserAnswersCacheConnector, times(1)).save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any(), any())
        verify(mockUserAnswersCacheConnector, times(1))
          .changeVersion(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq("1"), ArgumentMatchers.eq("2"))(any(), any(), any())
        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(2, Compiled))
        val actualOverviewValues = actualUAAfterSave.get(EventReportingOverviewPage)
          .flatMap {
            _.headOption.map { o =>
              Tuple2(o.versionDetails.head.numberOfVersions, o.versionDetails.head.compiledVersionAvailable)
            }
          }
        actualOverviewValues mustBe Some(Tuple2(2, true))
      }
    }

    "must maintain version and status when already compiled" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val edi = EventDataIdentifier(Event1, taxYear, "1")
      when(mockEventReportingConnector.compileEvent(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi), any(), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))

      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Compiled), nonEventTypeData = true)

      whenReady(compileService.compileEvent(Event1, pstr, ua)) { _ =>
        verify(mockUserAnswersCacheConnector, times(1))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any(), any())
        verify(mockEventReportingConnector, times(1))
          .compileEvent(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi), ArgumentMatchers.eq(1), ArgumentMatchers.eq(false))(any(), any())

        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(1, Compiled))
      }
    }

    "must throw exception when no version info is available" in {
      val ua = emptyUserAnswersWithTaxYear // No VersionInfoPage set

      recoverToSucceededIf[RuntimeException] {
        compileService.compileEvent(Event1, pstr, ua)
      }.map { _ =>
        verify(mockUserAnswersCacheConnector, times(0)).save(any(), any())(any(), any(), any())
        verify(mockEventReportingConnector, times(0)).compileEvent(any(), any(), any(), any())(any(), any())
        succeed
      }
    }

    "must not compile when submitted and data not modified" in {
      val edi = EventDataIdentifier(Event1, taxYear, "1")
      when(mockUserAnswersCacheConnector.isDataModified(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi.eventType))(any(), any(), any()))
        .thenReturn(Future.successful(Some(false)))

      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Submitted), nonEventTypeData = true)

      whenReady(compileService.compileEvent(Event1, pstr, ua)) { _ =>
        verify(mockUserAnswersCacheConnector, times(0)).save(any(), any())(any(), any(), any())
        verify(mockEventReportingConnector, times(0)).compileEvent(any(), any(), any(), any())(any(), any())
        verify(mockUserAnswersCacheConnector, times(1)).isDataModified(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(Event1))(any(), any(), any())
        succeed
      }
    }

    "must handle compile with delete flag set to true" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val edi = EventDataIdentifier(Event1, taxYear, "1")
      when(mockEventReportingConnector.compileEvent(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi), any(), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))

      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Compiled), nonEventTypeData = true)

      whenReady(compileService.compileEvent(Event1, pstr, ua, delete = true)) { _ =>
        verify(mockUserAnswersCacheConnector, times(1))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any(), any())
        verify(mockEventReportingConnector, times(1))
          .compileEvent(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi), ArgumentMatchers.eq(1), ArgumentMatchers.eq(true))(any(), any())

        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(1, Compiled))
        succeed
      }
    }
  }
}

