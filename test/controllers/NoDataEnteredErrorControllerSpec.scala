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
import connectors.SessionDataCacheConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers.{running, _}
import views.html.IndexView

import scala.concurrent.Future

class NoDataEnteredErrorControllerSpec extends SpecBase with MockitoSugar {

  private val mockSessionDataCacheConnector = mock[SessionDataCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[SessionDataCacheConnector].toInstance(mockSessionDataCacheConnector)
  )

  "No Data Entered Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      when(mockSessionDataCacheConnector.upsertTestPstr(any(), any())(any(), any())).thenReturn(Future.successful(()))

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndexView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
