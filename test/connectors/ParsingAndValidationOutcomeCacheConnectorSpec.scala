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
import models.fileUpload.ParsingAndValidationOutcome
import models.fileUpload.ParsingAndValidationOutcomeStatus._
import org.scalatest._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class ParsingAndValidationOutcomeCacheConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with OptionValues with RecoverMethods {

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  override protected def portConfigKey: String = "microservice.services.pension-scheme-event-reporting.port"

  private lazy val connector: ParsingAndValidationOutcomeCacheConnector = injector.instanceOf[ParsingAndValidationOutcomeCacheConnector]
  private val url = "/pension-scheme-event-reporting/parsing-and-validation-outcome"

  private val successOutcome = ParsingAndValidationOutcome(Success, fileName = Some("test"))

  ".getOutcome" must {

    "return `None` when there is no data in the collection" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            notFound
          )
      )

      connector.getOutcome map {
        result =>
          result mustNot be(defined)
      }
    }

    "return data if data is present in the collection" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            ok(Json.toJson(successOutcome).toString())
          )
      )

      connector.getOutcome map {
        result =>
          result.value mustEqual successOutcome
      }
    }

    "return `None` when the response is not OK" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            serverError() // Simulate an internal server error
          )
      )

      connector.getOutcome.map { result =>
        result mustNot be(defined)
      }
    }

    "handle NotFoundException gracefully" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            notFound() // Simulating a NotFoundException
          )
      )

      connector.getOutcome.map { result =>
        result mustNot be(defined)
      }
    }
  }

  ".setOutcome" must {
    val json = Json.toJson(successOutcome)

    "save the data in the collection" in {
      server.stubFor(
        post(urlEqualTo(url))
          .withRequestBody(equalTo(Json.stringify(json)))
          .willReturn(
            aResponse.withStatus(201)
          )
      )

      connector.setOutcome(successOutcome) map {
        _ mustEqual(():Unit)
      }
    }

    "log a warning if the outcome cannot be saved" in {
      val json = Json.toJson(successOutcome)

      server.stubFor(
        post(urlEqualTo(url))
          .withRequestBody(equalTo(Json.stringify(json)))
          .willReturn(
            serverError()
          )
      )

      connector.setOutcome(successOutcome).map {
        _ mustEqual((): Unit)
      }
    }
  }

  ".deleteOutcome" must {
    "return OK after removing all the data from the collection" in {
      server.stubFor(delete(urlEqualTo(url)).
        willReturn(ok)
      )
      connector.deleteOutcome.map {
        _ mustEqual(():Unit)
      }
    }

    "log a warning if the outcome cannot be deleted" in {
      server.stubFor(
        delete(urlEqualTo(url))
          .willReturn(
            serverError()
          )
      )

      connector.deleteOutcome.map {
        _ mustEqual((): Unit)
      }
    }
  }
}
