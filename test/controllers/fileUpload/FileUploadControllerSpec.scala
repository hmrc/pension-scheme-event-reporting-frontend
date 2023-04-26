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
import connectors.UserAnswersCacheConnector
import forms.fileUpload.FileUploadFormProvider
import models.enumeration.EventType.{Event22, getEventTypeByName}
import models.fileUpload.FileUpload
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fileUpload.FileUploadView

class FileUploadControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new FileUploadFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.FileUploadController.onPageLoad(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "onPageLoad" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, getEventTypeByName(Event22), Event22, Call("GET", "/"))(request, messages(application)).toString
      }
    }
  }
//
//  "showResult" - {
//
//    "must save file name to mongo if successful" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, showResultRoute)
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[FileUploadView]
//
//        status(result) mustEqual SEE_OTHER
//        verify(mockUserAnswersCacheConnector, times(1)).save(ArgumentMatchers.eq("123456"), ArgumentMatchers.eq(Event22), any())(any(), any())
//      }
//    }
//  }
}
