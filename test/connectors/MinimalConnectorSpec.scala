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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.http._
import utils.WireMockHelper

class MinimalConnectorSpec
  extends AsyncWordSpec
    with Matchers
    with WireMockHelper {

  import MinimalConnector._
  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private val psaIdName = "psaId"
  private val psaId = "A2100005"

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  private lazy val connector: MinimalConnector = injector.instanceOf[MinimalConnector]
  private val minimalPsaDetailsUrl = "/pension-administrator/get-minimal-psa"
  private val email = "test@test.com"

  private def validResponse(b:Boolean) =
    Json.stringify(
      Json.obj(
        "email" -> email,
        "isPsaSuspended" -> b,
        "organisationName" -> "test ltd",
        "deceasedFlag" -> false,
        "rlsFlag" -> false
      )
    )

  "getMinimalDetails" must {

    "return successfully when the backend has returned OK and a false response" in {
      server.stubFor(
        get(urlEqualTo(minimalPsaDetailsUrl))
          .willReturn(
            ok(validResponse(false))
              .withHeader("Content-Type", "application/json")
          )
      )

      connector.getMinimalDetails(psaIdName, psaId) map {
        _ mustBe MinimalDetails(email, isPsaSuspended = false, Some("test ltd"), None, rlsFlag = false, deceasedFlag = false)
      }
    }

    "return successfully when the backend has returned OK and a true response" in {
      server.stubFor(
        get(urlEqualTo(minimalPsaDetailsUrl))
          .willReturn(
            ok(validResponse(true))
              .withHeader("Content-Type", "application/json")
          )
      )

      connector.getMinimalDetails(psaIdName, psaId) map {
        _ mustBe MinimalDetails(email, isPsaSuspended = true, Some("test ltd"), None, rlsFlag = false, deceasedFlag = false)
      }
    }

    "return BadRequestException when the backend has returned anything other than ok" in {
      server.stubFor(
        get(urlEqualTo(minimalPsaDetailsUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[BadRequestException] {
        connector.getMinimalDetails(psaIdName, psaId)
      }
    }
  }
}
