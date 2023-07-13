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

import audit.{AuditService, EventReportingUpscanFileUploadAuditEvent}
import com.github.tomakehurst.wiremock.client.WireMock._
import config.FrontendAppConfig
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.enumeration.{Enumerable, EventType}
import models.requests.DataRequest
import models.{LoggedInUser, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doNothing, never, times, verify}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.Inside.inside
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.{OptionValues, RecoverMethods}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.http.Status.OK
import play.api.inject
import play.api.inject.guice.GuiceableModule
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.WireMockHelper

class UpscanInitiateConnectorSpec
  extends AsyncWordSpec
    with Matchers
    with WireMockHelper
    with MockitoSugar
    with OptionValues
    with RecoverMethods
    with Enumerable.Implicits {

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKey: String = "microservice.services.upscan-initiate.port"

  implicit val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockAuditService: AuditService = mock[AuditService]

  private lazy val connector: UpscanInitiateConnector = app.injector.instanceOf[UpscanInitiateConnector]
  private implicit val dataRequest: DataRequest[AnyContent] =
    DataRequest("Pstr123", "SchemeABC", "returnUrl", FakeRequest(GET, "/"), LoggedInUser("user", Administrator, "psaId"), UserAnswers())

  private val url = "/upscan/v2/initiate"

  override def beforeEach(): Unit = {
    Mockito.reset(mockAuditService)
    super.beforeEach()
  }

  override protected def bindings: Seq[GuiceableModule] = Seq[GuiceableModule](
    inject.bind[AuditService].toInstance(mockAuditService)
  )

  ".initiateV2" must {
    val successRedirectUrl = appConfig.successEndPointTarget(EventType.Event22)

    val errorRedirectUrl = appConfig
      .failureEndPointTarget(EventType.Event22)

    val response1 =
      s"""{
         |    "reference": "11370e18-6e24-453e-b45a-76d3e32ea33d",
         |    "uploadRequest": {
         |        "href": "https://xxxx/upscan-upload-proxy/bucketName",
         |        "fields": {
         |            "acl": "private",
         |            "key": "11370e18-6e24-453e-b45a-76d3e32ea33d",
         |            "policy": "xxxxxxxx==",
         |            "x-amz-algorithm": "AWS4-HMAC-SHA256",
         |            "x-amz-credential": "ASIAxxxxxxxxx/20180202/eu-west-2/s3/aws4_request",
         |            "x-amz-date": "yyyyMMddThhmmssZ",
         |            "x-amz-meta-callback-url": "https://myservice.com/callback",
         |            "x-amz-signature": "xxxx",
         |            "success_action_redirect": "https://myservice.com/nextPage",
         |            "error_action_redirect": "https://myservice.com/errorPage"
         |        }
         |    }
         |}""".stripMargin

    "return successful initiate upscan result" in {
      doNothing().when(mockAuditService).sendEvent(any())(any(), any())
      server.stubFor(
        post(urlEqualTo(url))
          .willReturn(
            aResponse.withBody(response1).withStatus(OK)
          )
      )
      connector.initiateV2(Some(successRedirectUrl), Some(errorRedirectUrl), EventType.Event22)(dataRequest, hc) map { result =>
        verify(mockAuditService, never()).sendEvent(any())(any(), any())
        result.fileReference.reference mustEqual "11370e18-6e24-453e-b45a-76d3e32ea33d"
        result.formFields.get("success_action_redirect") mustEqual Some("https://myservice.com/nextPage")
      }
    }

    "return failure initiate upscan result" in {
      val captor: ArgumentCaptor[EventReportingUpscanFileUploadAuditEvent] =
        ArgumentCaptor.forClass(classOf[EventReportingUpscanFileUploadAuditEvent])

      server.stubFor(
        post(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(400)
              .withBody("test")
          )
      )

      recoverToExceptionIf[UpstreamErrorResponse] {
        connector.initiateV2(Some(successRedirectUrl), Some(errorRedirectUrl), EventType.Event22)(dataRequest, hc)
      } map { ex =>
        verify(mockAuditService, times(1)).sendEvent(captor.capture())(any(), any())
        ex.statusCode mustEqual Status.BAD_REQUEST
        inside(captor.getValue) {
          case EventReportingUpscanFileUploadAuditEvent(eventType, psaOrPspId, pstr, schemeAdministratorType, outcome, _) =>
            eventType mustBe EventType.Event22
            psaOrPspId mustBe dataRequest.loggedInUser.psaIdOrPspId
            pstr mustBe dataRequest.pstr
            schemeAdministratorType mustBe dataRequest.loggedInUser.administratorOrPractitioner
            outcome.left.getOrElse("") contains "returned 400. Response body: 'test'" mustBe true
        }
      }
    }
  }
}
