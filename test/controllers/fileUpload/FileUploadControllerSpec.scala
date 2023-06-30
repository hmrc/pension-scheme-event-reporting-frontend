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
import connectors.{UpscanInitiateConnector, UserAnswersCacheConnector}
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23, Event6, getEventTypeByName}
import models.{UpscanFileReference, UpscanInitiateResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fileUpload.FileUploadView

import scala.concurrent.Future

class FileUploadControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints
  private val seqOfEvents = Seq(Event6, Event22, Event23)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockUpscanInitiateConnector = mock[UpscanInitiateConnector]

  private def getRoute(eventType: EventType): String = routes.FileUploadController.onPageLoad(waypoints, eventType).url

  private val formFieldsMap = Map(
    "testField1" -> "value1",
    "testField2" -> "value2"
  )

  private val upscanInitiateResponse = UpscanInitiateResponse(
    fileReference = UpscanFileReference(""),
    postTarget = "/postTarget",
    formFields = formFieldsMap
  )

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[UpscanInitiateConnector].toInstance(mockUpscanInitiateConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
    reset(mockUpscanInitiateConnector)
  }

  "FileUpload Controller" - {
    for (event <- seqOfEvents) {
      testReturnOkAndCorrectView(event)
    }
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET (Event ${eventType.toString})" in {

      when(mockUpscanInitiateConnector.initiateV2(any(), any())(any(), any())).thenReturn(Future.successful(upscanInitiateResponse))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(waypoints, getEventTypeByName(eventType), eventType,
            Call("post", upscanInitiateResponse.postTarget), formFieldsMap, None)(request, messages(application)).toString
      }
    }
  }
}
