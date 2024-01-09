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

package controllers

import base.SpecBase
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class FileDownloadControllerSpec extends SpecBase {

  private val event22Header = Some("""attachment; filename="instructions-event-22-annual-allowance.ods"""")
  private val event23Header = Some("""attachment; filename="instructions-event-23-dual-annual-allowance.ods"""")

  "FileDownload Controller" - {
    "templateFile" - {
      testReturnOkAndCorrectTemplateFile(Event22)
      testReturnOkAndCorrectTemplateFile(Event23)
    }
    "instructionsFile" - {
      testReturnOkAndCorrectInstructionsFile(Event22, event22Header)
      testReturnOkAndCorrectInstructionsFile(Event23, event23Header)
    }
  }

  private def testReturnOkAndCorrectTemplateFile(eventType: EventType): Unit = {
    s"return an OK with a GET for event ${eventType.toString} What you will need" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.FileDownloadController.templateFile(eventType).url)

      val result = route(application, request).value

      status(result) mustEqual OK

      whenReady(result) { response =>
        val headers: Option[String] = response.header.headers.get("Content-Disposition")

        headers mustBe Some(s"""attachment; filename="event-${eventType.toString}-bulk-upload.csv"""")
      }

      application.stop()
    }
  }

  private def testReturnOkAndCorrectInstructionsFile(eventType: EventType, header: Some[String]): Unit = {
    s"return an OK with a GET for event ${eventType.toString} What you will need" in {
      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.FileDownloadController.instructionsFile(eventType).url)

      val result = route(application, request).value

      status(result) mustEqual OK

      whenReady(result) { response =>
        val headers: Option[String] = response.header.headers.get("Content-Disposition")

        headers mustBe header
      }

      application.stop()
    }
  }
}