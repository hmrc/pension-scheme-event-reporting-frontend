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

package controllers

import base.SpecBase
import connectors.MinimalConnector
import connectors.MinimalConnector.MinimalDetails
import helpers.DateHelper
import helpers.DateHelper.dateFormatter
import models.{TaxYear, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{EmptyWaypoints, TaxYearPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ReturnSubmittedView

import scala.concurrent.Future

class ReturnSubmittedControllerSpec extends SpecBase {

  private val waypoints = EmptyWaypoints
  private val yourPensionSchemesUrl: String = "http://localhost:8204/manage-pension-schemes/your-pension-schemes"
  private val schemeName = "schemeName"
  private val taxYear = "2022 to 2023"
  private val dateHelper = new DateHelper
  private val dateSubmitted: String = dateHelper.now.format(dateFormatter)
  private val mockMinimalConnector = mock[MinimalConnector]

  "Return Submitted Controller" - {

    "must return OK and the correct view for a GET" in {
      val minimalDetails = MinimalDetails(email = "", isPsaSuspended = false,
        organisationName = None, individualDetails = None, rlsFlag = false, deceasedFlag = false)
      when(mockMinimalConnector.getMinimalDetails(any(), any())(any(), any())).thenReturn(Future.successful(minimalDetails))

      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2022"))
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {

        val request = FakeRequest(GET, routes.ReturnSubmittedController.onPageLoad(waypoints).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnSubmittedView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            routes.ReturnSubmittedController.onPageLoad(waypoints).url,
            yourPensionSchemesUrl,
            schemeName,
            taxYear,
            dateSubmitted)(request, messages(application)).toString
      }
    }
  }
}
