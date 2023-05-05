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
import models.enumeration.EventType.{Event22, getEventTypeByName}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fileUpload.FileUploadResultView

import scala.concurrent.Future

class FileUploadResultControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new FileUploadResultFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockERConnector = mock[EventReportingConnector]


  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[EventReportingConnector].toInstance(mockERConnector)
  )

  private def getRoute: String = routes.FileUploadResultController.onPageLoad(waypoints).url
  private def failureGetRoute: String = routes.FileRejectedController.onPageLoad(waypoints).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
    reset(mockERConnector)
  }

  "FileUploadResult Controller" - {

    "must return OK and the correct view for a GET when a file name is successfully retrieved from mongo cache" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      when(mockERConnector.getFileUploadOutcome(ArgumentMatchers.eq("123"))(any(), any()))
      .thenReturn(Future.successful(FileUploadOutcomeResponse(Some("testFile"), SUCCESS)))
      running(application) {
        val request = FakeRequest.apply(method = GET, path = getRoute + "?key=123")
        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadResultView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, getEventTypeByName(Event22), Some("testFile"))(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when a file name retrieval from mongo cache is in progress" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      when(mockERConnector.getFileUploadOutcome(ArgumentMatchers.eq("123"))(any(), any()))
        .thenReturn(Future.successful(FileUploadOutcomeResponse(None, IN_PROGRESS)))
      running(application) {
        val request = FakeRequest.apply(method = GET, path = getRoute + "?key=123")
        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadResultView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, getEventTypeByName(Event22), None)(request, messages(application)).toString
      }
    }

    "must return OK and redirect to the file rejected controller if file name is not retrieved from mongo cache" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      when(mockERConnector.getFileUploadOutcome(ArgumentMatchers.eq("123"))(any(), any()))
        .thenReturn(Future.successful(FileUploadOutcomeResponse(None, FAILURE)))
      running(application) {
        val request = FakeRequest.apply(method = GET, path = getRoute + "?key=123")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
    "must return BadRequest and redirect to the file rejected controller if upload id is not found in request" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()
      running(application) {
        val request = FakeRequest.apply(method = GET, path = getRoute)
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
//
//    "must populate the view correctly on a GET when the question has previously been answered" in {
//
//      val userAnswers = UserAnswers().set(FileUploadResultPage(Event22), FileUploadResult.values.head).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, getRoute)
//
//        val view = application.injector.instanceOf[FileUploadResultView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(form.fill(FileUploadResult.values.head), waypoints, getEventTypeByName(Event22), )(request, messages(application)).toString
//      }
//    }
//
//    "must save the answer and redirect to the next page when valid data is submitted" in {
//      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
//        .thenReturn(Future.successful(()))
//
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", FileUploadResult.values.head.toString))
//
//        val result = route(application, request).value
//        val updatedAnswers = emptyUserAnswers.set(FileUploadResultPage, FileUploadResult.values.head).success.value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual FileUploadResultPage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
//        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
//      }
//    }
//
//    "must return bad request when invalid data is submitted" in {
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))
//
//        val view = application.injector.instanceOf[FileUploadResultView]
//        val boundForm = form.bind(Map("value" -> "invalid"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//        contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
//        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
//      }
//    }
  }
}
