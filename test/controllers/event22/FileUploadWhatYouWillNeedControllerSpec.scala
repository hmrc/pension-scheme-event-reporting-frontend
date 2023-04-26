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

package controllers.event22

import base.SpecBase
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event22.FileUploadWhatYouWillNeedView

class FileUploadWhatYouWillNeedControllerSpec extends SpecBase {

  private def getRoute: String = routes.FileUploadWhatYouWillNeedController.onPageLoad().url

  //TODO: The continue URL needs to be changed for the future "next" page
  private def continueUrl: Call = controllers.routes.IndexController.onPageLoad

  private def templateDownloadLink: Call = controllers.routes.FileDownloadController.templateFile

  private def instructionsDownloadLink: Call = controllers.routes.FileDownloadController.instructionsFile

  "FileUploadWhatYouWillNeed Controller" - {


    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadWhatYouWillNeedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(continueUrl.url, templateDownloadLink, instructionsDownloadLink)(request, messages(application)).toString
      }
    }
  }
}
