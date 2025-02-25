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
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.requests.DataRequest
import models.{LoggedInUser, UserAnswers, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.VersionInfoPage
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.mvc.AnyContent
import play.api.mvc.Results.{BadRequest, Forbidden, InternalServerError, NoContent, ServiceUnavailable}
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//scalastyle:off magic.number
class SubmitServiceSpec extends SpecBase with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val pstr = "pstr"

  private val mockEventReportingConnector = mock[EventReportingConnector]
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private implicit val dataRequest: DataRequest[AnyContent] =
    DataRequest("Pstr123", "SchemeABC", "returnUrl", FakeRequest(GET, "/"), LoggedInUser("user", Administrator, "psaId"), UserAnswers(), "S2400000041")

  private def submitService = new SubmitService(mockEventReportingConnector, mockUserAnswersCacheConnector)

  override def beforeEach(): Unit = {
    reset(mockEventReportingConnector)
    reset(mockUserAnswersCacheConnector)
  }

  "submitReport" - {
    "must call connector with correct tax year and version when in progress (being compiled) setting status to submitted" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any(), any()))
        .thenReturn(Future.successful(NoContent))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        verify(mockUserAnswersCacheConnector, times(1))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any(), any())
        val actualUAAfterSave = captor.getValue
        actualUAAfterSave.get(VersionInfoPage) mustBe Some(VersionInfo(2, Submitted))
        status mustBe OK
      }
    }

    "must return Bad request when submitting event report multiple times in quick succession" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any(), any()))
        .thenReturn(Future.successful(BadRequest))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        verify(mockUserAnswersCacheConnector, times(0))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any(), any())
        status mustBe BAD_REQUEST
      }
    }

    "must return Bad request when an event report has already been submitted" in {
      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any(), any()))
        .thenReturn(Future.successful(BadRequest))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Submitted), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        verify(mockUserAnswersCacheConnector, times(0))
          .save(ArgumentMatchers.eq(pstr), captor.capture())(any(), any(), any())
        status mustBe BAD_REQUEST
      }
    }

    "must not call connector when status is submitted + return a not found" in {
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any(), any()))
        .thenReturn(Future.successful(NoContent))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Submitted), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        verify(mockUserAnswersCacheConnector, times(0))
          .save(ArgumentMatchers.eq(pstr), any())(any(), any(), any())
        status mustBe NOT_FOUND
      }
    }

    "must return NotFound when no version info is found" in {
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any(), any()))
        .thenReturn(Future.successful(NoContent))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear
      submitService.submitReport(pstr, ua).map { status =>
        verify(mockUserAnswersCacheConnector, times(0))
          .save(ArgumentMatchers.eq(pstr), any())(any(), any(), any())
        status mustBe NOT_FOUND
      }
    }

    "must return InternalServerError if the connector responds with an unexpected status" in {
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any(), any()))
        .thenReturn(Future.successful(InternalServerError))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        status mustBe InternalServerError
      }
    }

    "must return Forbidden if the connector returns a Forbidden status" in {
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any(), any()))
        .thenReturn(Future.successful(Forbidden))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        status mustBe Forbidden
      }
    }

    "must return ServiceUnavailable if the connector returns a SERVICE_UNAVAILABLE status" in {
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any(), any()))
        .thenReturn(Future.successful(ServiceUnavailable))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
        .thenReturn(Future.successful((): Unit))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        status mustBe ServiceUnavailable
      }
    }

    "must handle failures in saving user answers to cache" in {
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any(), any()))
        .thenReturn(Future.successful(NoContent))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
        .thenReturn(Future.failed(new RuntimeException("Database error")))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "must return InternalServerError if cache update fails after successful report submission" in {
      when(mockEventReportingConnector.submitReport(ArgumentMatchers.eq(pstr), any(), any())(any(), any()))
        .thenReturn(Future.successful(NoContent))
      when(mockUserAnswersCacheConnector.save(ArgumentMatchers.eq(pstr), any())(any(), any(), any()))
        .thenReturn(Future.failed(new RuntimeException("Cache save error")))
      val ua = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(2, Compiled), nonEventTypeData = true)
      submitService.submitReport(pstr, ua).map { status =>
        status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
