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
import connectors.UserAnswersCacheConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class KeepAliveControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterAll {

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  override def beforeAll(): Unit = {
    when(mockUserAnswersCacheConnector.postRefreshExpire(any(), any())).thenReturn(Future.successful(true))
  }

  "keepAlive" - {

    "when the user has answered some questions" - {

      "must keep the answers alive and return OK" in {
        val application =
          applicationBuilder(Some(emptyUserAnswers), Seq(inject.bind[UserAnswersCacheConnector].to(mockUserAnswersCacheConnector)))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }

    "when the user has not answered any questions" - {

      "must return OK" in {


        val application =
          applicationBuilder(None, Seq(inject.bind[UserAnswersCacheConnector].to(mockUserAnswersCacheConnector)))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }
  }
}
