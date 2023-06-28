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

package services

import base.SpecBase
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.enumeration.EventType.Event1
import models.enumeration.VersionStatus.{Compiled, NotStarted, Submitted}
import models.{EventDataIdentifier, UserAnswers, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.VersionInfoPage
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//scalastyle:off magic.number
class CompileServiceSpec extends SpecBase with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val pstr = "pstr"
  private val taxYear = "2022"

  private val mockEventReportingConnector = mock[EventReportingConnector]
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private def compileService = new CompileService(mockEventReportingConnector, mockUserAnswersCacheConnector)

  override def beforeEach(): Unit = {
    reset(mockEventReportingConnector)
    reset(mockUserAnswersCacheConnector)
  }

  "compileEvent" - {
    "must call connector with correct tax year and version when not started" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val edi = EventDataIdentifier(Event1, taxYear, "1")
      when(mockEventReportingConnector.compileEvent(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi))(any, any()))
        .thenReturn(Future.successful(():Unit))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(Event1), any())(any(), any()))
        .thenReturn(Future.successful(():Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, NotStarted), nonEventTypeData = true)
      compileService.compileEvent(Event1, pstr, ua).map { _ =>
        verify(mockUserAnswersCacheConnector, times(1))
          .save(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(Event1), captor.capture())(any(), any())
        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(1, Compiled))
        assert(true)
      }
    }

    "must call connector with correct tax year and version when in progress (being compiled)" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val edi = EventDataIdentifier(Event1, taxYear, "2")
      when(mockEventReportingConnector.compileEvent(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi))(any, any()))
        .thenReturn(Future.successful((): Unit))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(Event1), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      compileService.compileEvent(Event1, pstr, ua).map { _ =>
        verify(mockUserAnswersCacheConnector, times(1))
          .save(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(Event1), captor.capture())(any(), any())
        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(2, Compiled))
        assert(true)
      }
    }

    "must call connector with correct tax year and version when in submitted" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      val edi = EventDataIdentifier(Event1, taxYear, "1")
      when(mockEventReportingConnector.compileEvent(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(edi))(any, any()))
        .thenReturn(Future.successful((): Unit))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(Event1), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Submitted), nonEventTypeData = true)
      compileService.compileEvent(Event1, pstr, ua).map { _ =>
        verify(mockUserAnswersCacheConnector, times(1))
          .save(ArgumentMatchers.eq(pstr), ArgumentMatchers.eq(Event1), captor.capture())(any(), any())
        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(2, Compiled))
        assert(true)
      }
    }
  }
}

