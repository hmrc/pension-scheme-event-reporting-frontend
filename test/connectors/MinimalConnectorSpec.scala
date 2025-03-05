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

import com.fasterxml.jackson.core.JsonParseException
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

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  private lazy val connector: MinimalConnector = injector.instanceOf[MinimalConnector]
  private val minimalPsaDetailsUrl = "/pension-administrator/get-minimal-details-self"
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

  "MinimalDetails" should {

    "return the organisation name when individualDetails is None and organisationName is defined" in {
      val minimalDetails = MinimalDetails(
        email = email,
        isPsaSuspended = false,
        organisationName = Some("test ltd"),
        individualDetails = None,
        rlsFlag = false,
        deceasedFlag = false
      )

      minimalDetails.name mustBe "test ltd"
    }

    "return the individual's full name when organisationName is None and individualDetails is defined" in {
      val individualDetails = IndividualDetails(
        firstName = "John",
        middleName = Some("Edward"),
        lastName = "Doe"
      )

      val minimalDetails = MinimalDetails(
        email = email,
        isPsaSuspended = false,
        organisationName = None,
        individualDetails = Some(individualDetails),
        rlsFlag = false,
        deceasedFlag = false
      )

      minimalDetails.name mustBe "John Edward Doe"
    }

    "throw an exception when both organisationName and individualDetails are None" in {
      val minimalDetails = MinimalDetails(
        email = email,
        isPsaSuspended = false,
        organisationName = None,
        individualDetails = None,
        rlsFlag = false,
        deceasedFlag = false
      )

      intercept[RuntimeException] {
        minimalDetails.name
      }.getMessage mustBe "Cannot find name"
    }

    "return the full name without a middle name when middleName is None" in {
      val individualDetails = IndividualDetails(
        firstName = "Jane",
        middleName = None,
        lastName = "Smith"
      )

      individualDetails.fullName mustBe "Jane Smith"
    }
  }

  "getMinimalDetails" must {

    "return successfully when the backend has returned OK and a false response" in {
      server.stubFor(
        get(urlEqualTo(minimalPsaDetailsUrl))
          .willReturn(
            ok(validResponse(false))
              .withHeader("Content-Type", "application/json")
          )
      )

      connector.getMinimalDetails(psaIdName) map {
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

      connector.getMinimalDetails(psaIdName) map {
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
        connector.getMinimalDetails(psaIdName)
      }
    }

    "handle deserialization error when invalid JSON is returned" in {
      server.stubFor(
        get(urlEqualTo(minimalPsaDetailsUrl))
          .willReturn(
            ok("{invalid-json}")
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[JsonParseException] {
        connector.getMinimalDetails(psaIdName)
      }
    }

    "throw DelimitedAdminException when the response body contains 'DELIMITED_PSAID'" in {
      server.stubFor(
        get(urlEqualTo(minimalPsaDetailsUrl))
          .willReturn(
            forbidden
              .withBody("DELIMITED_PSAID")
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[DelimitedAdminException] {
        connector.getMinimalDetails(psaIdName)
      }
    }

    "handle unexpected error codes gracefully" in {
      server.stubFor(
        get(urlEqualTo(minimalPsaDetailsUrl))
          .willReturn(
            serverError
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getMinimalDetails(psaIdName)
      }
    }
  }
}
