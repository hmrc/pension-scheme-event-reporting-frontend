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
import connectors.UserAnswersCacheConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.CannotResumeView

import scala.concurrent.Future

class CannotResumeControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  override def beforeEach(): Unit = {
    reset(mockUserAnswersCacheConnector)
  }

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  "CannotResume Controller" - {

    "must return OK and the correct view for a GET" in {
      when(mockUserAnswersCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(()))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()

      running(application) {

        val request = FakeRequest(GET, routes.CannotResumeController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CannotResumeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
