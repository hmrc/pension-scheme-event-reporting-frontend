/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.auth

import base.SpecBase
import config.FrontendAppConfig
import connectors.SessionDataCacheConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{reset, times, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.inject
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.net.URLEncoder
import scala.concurrent.Future

class AuthControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  // TODO: Amend once auth sorted

  private val mockSessionDataCacheConnector = mock[SessionDataCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    inject.bind[SessionDataCacheConnector].toInstance(mockSessionDataCacheConnector)
  )

  override def beforeEach: Unit = {
    reset(mockSessionDataCacheConnector)
    when(mockSessionDataCacheConnector.removeAll(any())(any(), any()))
      .thenReturn(Future.successful(Ok("")))
  }

  "signOut" - {

    "must redirect to the continue URL and clear down session cache" in {
      val application =
        applicationBuilder(None, extraModules)
          .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.AuthController.signOut.url)

        val result = route(application, request).value

        val encodedContinueUrl  = URLEncoder.encode(appConfig.exitSurveyUrl, "UTF-8")
        val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
        verify(mockSessionDataCacheConnector, times(1)).removeAll(any())(any(), any())
      }
    }
  }

  "signOutNoSurvey" - {
    "must redirect to sign out, specifying SignedOut as the continue URL and clear down session cache" in {
      val application =
        applicationBuilder(None, extraModules)
          .build()

      running(application) {

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.AuthController.signOutNoSurvey.url)

        val result = route(application, request).value

        val encodedContinueUrl  = URLEncoder.encode(routes.SignedOutController.onPageLoad.url, "UTF-8")
        val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
        verify(mockSessionDataCacheConnector, times(1)).removeAll(any())(any(), any())
      }
    }
  }
}
