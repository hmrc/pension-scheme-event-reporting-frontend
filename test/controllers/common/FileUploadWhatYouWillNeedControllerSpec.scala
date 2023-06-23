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

package controllers.common

import base.SpecBase
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
import pages.EmptyWaypoints
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.common.FileUploadWhatYouWillNeedView

class FileUploadWhatYouWillNeedControllerSpec extends SpecBase {

  private val seqOfEvents = Seq(Event22, Event23)

  private def getRoute(eventType: EventType): String = controllers.common.routes.FileUploadWhatYouWillNeedController.onPageLoad(EmptyWaypoints, eventType).url

  //TODO: The continue URL needs to be changed for the future "next" page
  private def continueUrl: Call = controllers.routes.IndexController.onPageLoad

  private def templateDownloadLink(eventType: EventType): Call = controllers.routes.FileDownloadController.templateFile(eventType)

  private def instructionsDownloadLink(eventType: EventType): Call = controllers.routes.FileDownloadController.instructionsFile(eventType)


  "FileUploadWhatYouWillNeed Controller" - {

    for (event <- seqOfEvents) {
      testReturnOkAndCorrectView(event)
    }
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET for Event $eventType" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, getRoute(eventType))

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadWhatYouWillNeedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(continueUrl.url, templateDownloadLink(eventType), instructionsDownloadLink(eventType), eventType)(request, messages(application)).toString
      }
    }
  }
}
