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
import models.enumeration.EventType.Event22
import pages.EmptyWaypoints
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fileUpload.FileUploadSuccessView

class FileUploadSuccessControllerSpec extends SpecBase {

  "FileUploadSuccess Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear)).build()
      val continueUrl = controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, Event22).url
      val fileName = "Dummy filename.csv"

      running(application) {

        val request = FakeRequest(GET, routes.FileUploadSuccessController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadSuccessView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(continueUrl, fileName)(request, messages(application)).toString
      }
    }
  }
}
