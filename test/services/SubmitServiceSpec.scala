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
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.{UserAnswers, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.VersionInfoPage
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.mvc.Results.{BadRequest, NoContent}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//scalastyle:off magic.number
class SubmitServiceSpec extends SpecBase with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val pstr = "pstr"

  private val mockEventReportingConnector = mock[EventReportingConnector]
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def submitService = new SubmitService(mockEventReportingConnector, mockUserAnswersCacheConnector)

  override def beforeEach(): Unit = {
    reset(mockEventReportingConnector)
    reset(mockUserAnswersCacheConnector)
  }

  "submitReport" - {
    "must call connector with correct tax year and version when in progress (being compiled) setting status to submitted" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any, any()))
        .thenReturn(Future.successful(NoContent))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        verify(mockUserAnswersCacheConnector, times(1))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any())
        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(2, Submitted))
        status mustBe OK
      }
    }

    "must return Bad request when submitting event report multiple times in quick succession" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any, any()))
        .thenReturn(Future.successful(BadRequest))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        verify(mockUserAnswersCacheConnector, times(0))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any())
        status mustBe BAD_REQUEST
      }
    }

    "must return Bad request when an event report has already been submitted" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any, any()))
        .thenReturn(Future.successful(BadRequest))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Submitted), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        verify(mockUserAnswersCacheConnector, times(0))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any())
        status mustBe BAD_REQUEST
      }
    }

    "must not call connector when status is submitted + return a not found" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any, any()))
        .thenReturn(Future.successful(NoContent))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Submitted), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        verify(mockUserAnswersCacheConnector, times(0))
          .save(ArgumentMatchers.eq(pstr), any())(any(), any())
        status mustBe NOT_FOUND
      }
    }
  }
}

