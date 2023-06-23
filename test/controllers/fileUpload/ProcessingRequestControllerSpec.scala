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
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
import pages.EmptyWaypoints
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fileUpload.ProcessingRequestView

class ProcessingRequestControllerSpec extends SpecBase {

  private val waypoints = EmptyWaypoints
  private val seqOfEvents = Seq(Event22, Event23)

  "ProcessingRequest Controller" - {
    for (event <- seqOfEvents) {
      testReturnOkAndCorrectView(event)
    }
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET (Event ${eventType.toString})" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.ProcessingRequestController.onPageLoad(waypoints, eventType).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProcessingRequestView]

        contentAsString(result) mustEqual view(controllers.routes.IndexController.onPageLoad.url)(request, messages(application)).toString
      }
    }
  }

}
