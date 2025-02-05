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
import models.PsaSchemeDetails
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.http.Status.OK
import uk.gov.hmrc.http._
import utils.WireMockHelper

import java.time.LocalDate

class SchemeConnectorSpec
  extends AsyncWordSpec
    with Matchers
    with WireMockHelper {
  override protected def portConfigKey: String = "microservice.services.pensions-scheme.port"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  private val psaId = "0000"
  private val pspId = "000012"
  private val pstr = "pstr"
  private val idNumber = "00000000AA"


  "getOpenDate" must {
    val openDateUrl = "/pensions-scheme/open-date"
    "return the openDate for a valid request/response" in {
      server.stubFor(
        get(urlEqualTo(openDateUrl))
          .withHeader("idType", equalTo("psaid"))
          .withHeader("idValue", equalTo(psaId))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody("2017-12-17")
          )
      )

      val connector = injector.instanceOf[SchemeConnector]

      connector.getOpenDate("psaId", psaId, pstr).map { openDate =>
        openDate mustBe LocalDate.parse("2017-12-17")
      }
    }

    "return the openDate for psp for a valid request/response" in {
      server.stubFor(
        get(urlEqualTo(openDateUrl))
          .withHeader("idType", equalTo("pspid"))
          .withHeader("idValue", equalTo(pspId))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody("2017-12-17")
          )
      )

      val connector = injector.instanceOf[SchemeConnector]

      connector.getOpenDate("pspId", pspId, pstr).map { openDate =>
        openDate mustBe LocalDate.parse("2017-12-17")
      }
    }

    "throw BadRequestException for a 400 Bad Request response" in {
      server.stubFor(
        get(urlEqualTo(openDateUrl))
          .withHeader("idType", equalTo("psaid"))
          .withHeader("idValue", equalTo(psaId))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      val connector = injector.instanceOf[SchemeConnector]

      recoverToSucceededIf[BadRequestException] {
        connector.getOpenDate("psaId", psaId, pstr)
      }
    }
  }

  "getSchemeDetails" must {
    val schemeDetailsUrl = s"/pensions-scheme/scheme/$idNumber"
    "return the SchemeDetails for a valid request/response" in {
      val jsonResponse = """{"schemeName":"test scheme", "pstr": "test pstr", "schemeStatus": "test status"}"""
      server.stubFor(
        get(urlEqualTo(schemeDetailsUrl))
          .withHeader("idNumber", equalTo(idNumber))
          .withHeader("psaId", equalTo(psaId))
          .withHeader("schemeIdType", equalTo("pstr"))
          .willReturn(ok(jsonResponse)
            .withHeader("Content-Type", "application/json")
          )
      )

      val connector = injector.instanceOf[SchemeConnector]

      connector.getSchemeDetails(psaId, idNumber, "pstr").map(schemeDetails =>
        schemeDetails mustBe PsaSchemeDetails("test scheme", "test pstr", "test status", None)
      )
    }

    "throw BadRequestException for a 400 Bad Request response" in {
      server.stubFor(
        get(urlEqualTo(schemeDetailsUrl))
          .withHeader("idNumber", equalTo(idNumber))
          .withHeader("psaId", equalTo(psaId))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      val connector = injector.instanceOf[SchemeConnector]

      recoverToSucceededIf[BadRequestException] {
        connector.getSchemeDetails(psaId, idNumber, "srn")
      }
    }
  }
}
