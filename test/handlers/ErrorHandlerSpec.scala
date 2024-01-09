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

package handlers

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, _}
import views.html.NoDataEnteredErrorView

class ErrorHandlerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  "ErrorHandlerSpec" - {

    "must catch ExpectationFailedExceptions and return the correct view" in {
      val application = applicationBuilder(userAnswers = None).build()

      val config = application.injector.instanceOf[FrontendAppConfig]

      val errorHandler = application.injector.instanceOf[ErrorHandler]

      val request = FakeRequest(GET, routes.DeclarationController.onClick(waypoints).url)

      val exception = new NothingToSubmitException("User data not available")

      val result = errorHandler.onServerError(request, exception)

      val view = application.injector.instanceOf[NoDataEnteredErrorView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(config.manageOverviewDashboardUrl)(request, messages(application)).toString
    }
  }
}