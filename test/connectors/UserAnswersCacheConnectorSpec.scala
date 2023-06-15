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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import models.UserAnswers
import models.enumeration.EventType
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.http._
import utils.WireMockHelper

class UserAnswersCacheConnectorSpec
  extends AsyncWordSpec
    with Matchers
    with WireMockHelper {

  private val pstr = "87219363YN"
  private val eventType = EventType.Event1

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKey: String = "microservice.services.pension-scheme-event-reporting.port"

  private lazy val connector: UserAnswersCacheConnector = injector.instanceOf[UserAnswersCacheConnector]
  private val userAnswersCacheUrl = s"/pension-scheme-event-reporting/user-answers"

  private val validResponse =
    Json.obj(
      "test" -> "test"
    )

  private val userAnswers = UserAnswers(validResponse)


  "get" must {
    "return successfully when the backend has returned OK and a correct response" in {
      server.stubFor(
        get(urlEqualTo(userAnswersCacheUrl))
          .willReturn(
            ok(Json.stringify(validResponse))
              .withHeader("Content-Type", "application/json")
          )
      )

      connector.get(pstr, eventType) map {
        _ mustBe Some(UserAnswers(validResponse, validResponse))
      }
    }

    "return successfully when the backend has returned NOT FOUND and a correct response" in {
      server.stubFor(
        get(urlEqualTo(userAnswersCacheUrl))
          .willReturn(
            notFound
              .withHeader("Content-Type", "application/json")
          )
      )

      connector.get(pstr, eventType) map {
        _ mustBe None
      }
    }

    "return BadRequestException when the backend has returned bad request response" in {
      server.stubFor(
        get(urlEqualTo(userAnswersCacheUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.get(pstr, eventType)
      }
    }
  }

  "save" must {
    "return successfully when passed event type and the backend has returned OK and a correct response" in {
      server.stubFor(
        post(urlEqualTo(userAnswersCacheUrl))
          .withHeader("eventType", equalTo(eventType.toString))
          .withHeader("pstr", equalTo(pstr))
          .willReturn(
            ok()
          )
      )

      connector.save(pstr, eventType, userAnswers) map {
        _ mustBe ()
      }
    }

    "return successfully when passed no event type and the backend has returned OK and a correct response" in {
      server.stubFor(
        post(urlEqualTo(userAnswersCacheUrl))
          .withHeader("pstr", equalTo(pstr))
          .willReturn(
            ok()
          )
      )

      connector.save(pstr, userAnswers) map {
        _ mustBe()
      }
    }

    "return BadRequestException when the backend has returned bad request response" in {
      server.stubFor(
        post(urlEqualTo(userAnswersCacheUrl))
          .withHeader("eventType", equalTo(eventType.toString))
          .withHeader("pstr", equalTo(pstr))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.save(pstr, eventType, userAnswers)
      }
    }
  }

  "removeAll" must {
    "return successfully when passed pstr and the backend has returned OK and a correct response" in {
      server.stubFor(
        delete(urlEqualTo(userAnswersCacheUrl))
          .withHeader("pstr", equalTo(pstr))
          .willReturn(
            ok()
          )
      )

      connector.removeAll(pstr) map {
        _ mustBe()
      }
    }

    "return BadRequestException when the backend has returned bad request response" in {
      server.stubFor(
        delete(urlEqualTo(userAnswersCacheUrl))
          .withHeader("pstr", equalTo(pstr))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.removeAll(pstr)
      }
    }
  }
}
