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

package controllers.fileUpload

import base.SpecBase
import connectors.ParsingAndValidationOutcomeCacheConnector
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event23, Event24, Event6}
import models.fileUpload.{ParsingAndValidationOutcome, ParsingAndValidationOutcomeStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ProcessingRequestControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockParsingAndValidationOutcomeCacheConnector = mock[ParsingAndValidationOutcomeCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq(
    bind[ParsingAndValidationOutcomeCacheConnector].to(mockParsingAndValidationOutcomeCacheConnector)
  )
  private val waypoints = EmptyWaypoints
  private val seqOfEvents = Seq(Event1, Event6, Event22, Event23, Event24)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockParsingAndValidationOutcomeCacheConnector)
  }

  "ProcessingRequest Controller" - {
    seqOfEvents.map { event =>
      testReturnOkAndCorrectViewWhenOutcomeSuccess(event)
      testReturnOkAndCorrectViewWhenOutcomeGeneralError(event)
      testReturnOkAndCorrectViewWhenValidationErrorsLessThan10(event)
      testReturnOkAndCorrectViewWhenValidationErrorsMoreThanOrEqualTo10(event)
      testRedirectToInvalidHeadersOrEmptyFile(event)
      testReturnOkAndCorrectViewDefault(event)
    }
  }

  private def testReturnOkAndCorrectViewWhenOutcomeSuccess(eventType: EventType): Unit = {
    s"return OK and the correct view for a GET when outcome is Success and can get file name (Event${eventType.toString})" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      val testFile = "test-file.csv"
      when(mockParsingAndValidationOutcomeCacheConnector.getOutcome(any(), any()))
        .thenReturn(Future.successful(Some(ParsingAndValidationOutcome(status = ParsingAndValidationOutcomeStatus.Success, fileName = Some(testFile)))))

      val request = FakeRequest(GET, routes.ProcessingRequestController.onPageLoad(waypoints, eventType).url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.FileUploadSuccessController.onPageLoad(waypoints, eventType).url
      verify(mockParsingAndValidationOutcomeCacheConnector, times(1)).getOutcome(any(), any())

      Await.result(application.stop(), 10.seconds)
    }
  }

  private def testReturnOkAndCorrectViewWhenOutcomeGeneralError(eventType: EventType): Unit = {
    s"return OK and the correct view for a GET when outcome is GeneralError (Event${eventType.toString})" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      val testFile = "test-file.csv"
      when(mockParsingAndValidationOutcomeCacheConnector.getOutcome(any(), any()))
        .thenReturn(Future.successful(Some(ParsingAndValidationOutcome(status = ParsingAndValidationOutcomeStatus.GeneralError, fileName = Some(testFile)))))

      val request = FakeRequest(GET, routes.ProcessingRequestController.onPageLoad(waypoints, eventType).url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ProblemWithServiceController.onPageLoad(waypoints, eventType).url
      verify(mockParsingAndValidationOutcomeCacheConnector, times(1)).getOutcome(any(), any())

      Await.result(application.stop(), 10.seconds)
    }
  }

  private def testReturnOkAndCorrectViewWhenValidationErrorsLessThan10(eventType: EventType): Unit = {
    s"return OK and the correct view for a GET when outcome is ValidationErrorsLessThan10 (Event${eventType.toString})" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      val testFile = "test-file.csv"
      when(mockParsingAndValidationOutcomeCacheConnector.getOutcome(any(), any()))
        .thenReturn(Future.successful(Some(ParsingAndValidationOutcome(status = ParsingAndValidationOutcomeStatus.ValidationErrorsLessThan10, fileName = Some(testFile)))))

      val request = FakeRequest(GET, routes.ProcessingRequestController.onPageLoad(waypoints, eventType).url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ValidationErrorsAllController.onPageLoad(waypoints, eventType).url
      verify(mockParsingAndValidationOutcomeCacheConnector, times(1)).getOutcome(any(), any())

      Await.result(application.stop(), 10.seconds)
    }
  }

  private def testReturnOkAndCorrectViewWhenValidationErrorsMoreThanOrEqualTo10(eventType: EventType): Unit = {
    s"return OK and the correct view for a GET when outcome is ValidationErrorsMoreThanOrEqualTo10 (Event${eventType.toString})" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      val testFile = "test-file.csv"
      when(mockParsingAndValidationOutcomeCacheConnector.getOutcome(any(), any()))
        .thenReturn(Future.successful(Some(ParsingAndValidationOutcome(status = ParsingAndValidationOutcomeStatus.ValidationErrorsMoreThanOrEqual10, fileName = Some(testFile)))))

      val request = FakeRequest(GET, routes.ProcessingRequestController.onPageLoad(waypoints, eventType).url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ValidationErrorsSummaryController.onPageLoad(waypoints, eventType).url
      verify(mockParsingAndValidationOutcomeCacheConnector, times(1)).getOutcome(any(), any())

      Await.result(application.stop(), 10.seconds)
    }
  }

  private def testRedirectToInvalidHeadersOrEmptyFile(eventType: EventType): Unit = {
    s"redirect to InvalidHeadersOrEmptyFileController for a GET when outcome is IncorrectHeadersOrEmptyFile (Event${eventType.toString})" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      when(mockParsingAndValidationOutcomeCacheConnector.getOutcome(any(), any()))
        .thenReturn(Future.successful(Some(ParsingAndValidationOutcome(status = ParsingAndValidationOutcomeStatus.IncorrectHeadersOrEmptyFile, fileName = None))))

      val request = FakeRequest(GET, routes.ProcessingRequestController.onPageLoad(waypoints, eventType).url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.InvalidHeadersOrEmptyFileController.onPageLoad(waypoints, eventType).url
      verify(mockParsingAndValidationOutcomeCacheConnector, times(1)).getOutcome(any(), any())

      Await.result(application.stop(), 10.seconds)
    }
  }

  private def testReturnOkAndCorrectViewDefault(eventType: EventType): Unit = {
    s"return OK and the correct view for a GET when outcome is not matched (Event${eventType.toString})" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      when(mockParsingAndValidationOutcomeCacheConnector.getOutcome(any(), any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, routes.ProcessingRequestController.onPageLoad(waypoints, eventType).url)
      val result = route(application, request).value

      status(result) mustEqual OK
      verify(mockParsingAndValidationOutcomeCacheConnector, times(1)).getOutcome(any(), any())

      Await.result(application.stop(), 10.seconds)
    }
  }
}
