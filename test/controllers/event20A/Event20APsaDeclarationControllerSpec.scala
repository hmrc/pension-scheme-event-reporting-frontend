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

package controllers.event20A

import base.SpecBase
import pages.EmptyWaypoints
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event20A.Event20APsaDeclarationView

class Event20APsaDeclarationControllerSpec extends SpecBase {

  "Event20APsaDeclaration Controller" - {

    val schemeName = "Big Scheme"
    val pstr = "PSTR1234"
    val taxYear = "2021"
    val adminName = "John Smith"

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.Event20APsaDeclarationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[Event20APsaDeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          schemeName, pstr, taxYear, adminName, routes.Event20APsaDeclarationController.onPageLoad(EmptyWaypoints).url)(request, messages(application)
        ).toString
      }
    }
  }
}
