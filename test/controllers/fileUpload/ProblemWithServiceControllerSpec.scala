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
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event23, Event24, Event6}
import pages.EmptyWaypoints
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fileUpload.ProblemWithServiceView

class ProblemWithServiceControllerSpec extends SpecBase {

  private val seqOfEvents = Seq(Event1, Event6, Event22, Event23, Event24)

  "ProblemWithService Controller" - {
    for (event <- seqOfEvents) {
      testReturnOkAndCorrectView(event)
    }
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET (Event ${eventType.toString})" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear)).build()
      val returnUrl = controllers.fileUpload.routes.FileUploadController.onPageLoad(EmptyWaypoints, eventType).url

      running(application) {

        val request = FakeRequest(GET, routes.ProblemWithServiceController.onPageLoad(EmptyWaypoints, eventType).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProblemWithServiceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(returnUrl)(request, messages(application)).toString
      }
    }
  }
}
