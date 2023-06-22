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

package controllers.fileUpload

import base.SpecBase
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import forms.fileUpload.FileUploadResultFormProvider
import models.FileUploadOutcomeResponse
import models.FileUploadOutcomeStatus.{FAILURE, IN_PROGRESS, SUCCESS}
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23, getEventTypeByName}
import models.fileUpload.FileUploadResult
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import pages.fileUpload.FileUploadResultPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fileUpload.FileUploadResultView

import scala.concurrent.Future

class FileUploadResultControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new FileUploadResultFormProvider()
  private val form = formProvider()
  private val seqOfEvents = Seq(Event22, Event23)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockERConnector = mock[EventReportingConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[EventReportingConnector].toInstance(mockERConnector)
  )

  private def getRoute(eventType: EventType): String = routes.FileUploadResultController.onPageLoad(waypoints, eventType).url

  private def postRoute(eventType: EventType): String = routes.FileUploadResultController.onSubmit(waypoints, eventType).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
    reset(mockERConnector)
  }

  "FileUploadResult Controller" - {

    for (event <- seqOfEvents) {
      testReturnOkAndCorrectViewFileSuccessfullyRetrieved(event)
      testReturnOkAndCorrectViewFileRetrievalInProgress(event)
      testReturnOkAndRedirectFileRejected(event)
      testReturnBadRequestIfUploadIdNotFound(event)
      testSaveAnswerAndRedirectWhenValid(event)
      testBadRequestForInvalidDataSubmission(event)
    }
  }

  private def testReturnOkAndCorrectViewFileSuccessfullyRetrieved(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET when a file name is successfully retrieved from mongo cache (Event $eventType)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      when(mockERConnector.getFileUploadOutcome(ArgumentMatchers.eq("123"))(any(), any()))
        .thenReturn(Future.successful(FileUploadOutcomeResponse(Some("testFile"), SUCCESS, Some("downloadUrl"))))
      running(application) {
        val request = FakeRequest.apply(method = GET, path = getRoute(eventType) + "?key=123")
        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadResultView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, getEventTypeByName(eventType), Some("testFile"),
          Call("POST", postRoute(eventType) + "?key=123"))(request, messages(application)).toString
      }
    }
  }

  private def testReturnOkAndCorrectViewFileRetrievalInProgress(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET when a file name retrieval from mongo cache is in progress (Event $eventType)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      when(mockERConnector.getFileUploadOutcome(ArgumentMatchers.eq("123"))(any(), any()))
        .thenReturn(Future.successful(FileUploadOutcomeResponse(None, IN_PROGRESS, None)))
      running(application) {
        val request = FakeRequest.apply(method = GET, path = getRoute(eventType) + "?key=123")
        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadResultView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, getEventTypeByName(eventType),
          None, Call("POST", postRoute(eventType) + "?key=123"))(request, messages(application)).toString
      }
    }
  }

  private def testReturnOkAndRedirectFileRejected(eventType: EventType): Unit = {
    s"must return OK and redirect to the file rejected controller if file name is not retrieved from mongo cache (Event $eventType)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      when(mockERConnector.getFileUploadOutcome(ArgumentMatchers.eq("123"))(any(), any()))
        .thenReturn(Future.successful(FileUploadOutcomeResponse(None, FAILURE, None)))
      running(application) {
        val request = FakeRequest.apply(method = GET, path = getRoute(eventType) + "?key=123")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }

  private def testReturnBadRequestIfUploadIdNotFound(eventType: EventType): Unit = {
    s"must return BadRequest and redirect to the file rejected controller if upload id is not found in request (Event $eventType)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      running(application) {
        val request = FakeRequest.apply(method = GET, path = getRoute(eventType))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }

  private def testSaveAnswerAndRedirectWhenValid(eventType: EventType): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted (Event $eventType)" in {

      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest.apply(POST, path = postRoute(eventType)).withFormUrlEncodedBody(("value", FileUploadResult.values.head.toString))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(FileUploadResultPage(eventType), FileUploadResult.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual FileUploadResultPage(eventType).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }
  }

  private def testBadRequestForInvalidDataSubmission(eventType: EventType): Unit = {
    s"must return bad request when invalid data is submitted (Event $eventType)" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest.apply(method = POST, path = postRoute(eventType)).withFormUrlEncodedBody(("value", "invalid"))

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
