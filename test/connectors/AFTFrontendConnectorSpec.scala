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
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.http.Status
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.http.StringContextOps
import utils.WireMockHelper

class AFTFrontendConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with OptionValues {
  override protected def portConfigKey: String = "microservice.services.aft-frontend.port"
  private val erOutstandingPaymentAmountURL = "/manage-pension-scheme-accounting-for-tax/%s/er-outstanding-payment-amount"
  private val dueAmount: Html = Html("£0.0")
  private val srn = "srn"
  implicit val request: FakeRequest[_] = FakeRequest("", "")


  "AFTFrontendConnector" when {
    "asked to getErOutstandingPaymentAmount" should {
      "call the micro service with the correct uri and return event reporting overdue amount" in {
        server.stubFor(
          get(urlEqualTo(erOutstandingPaymentAmountURL.format(srn)))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withHeader("Content-Type", "application/json")
                .withBody(dueAmount.toString())
            )
        )

        val connector = injector.instanceOf[AFTFrontendConnector]

        connector.getErOutstandingPaymentAmount(srn).map(erAmount =>
          erAmount mustBe dueAmount
        )
      }
    }
  }
}
