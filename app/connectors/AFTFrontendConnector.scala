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

import com.google.inject.Inject
import config.FrontendAppConfig
import play.api.Logger
import play.api.mvc.Request
import play.twirl.api.Html
import services.HeaderCarrierFunctions
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.partials.HtmlPartial
import uk.gov.hmrc.play.partials.HtmlPartial.connectionExceptionsAsHtmlPartialFailure

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class AFTFrontendConnector @Inject()(httpClientV2: HttpClientV2, config: FrontendAppConfig) {

  private val logger = Logger(classOf[AFTFrontendConnector])

  def getErOutstandingPaymentAmount[A](srn: String)
                           (implicit request: Request[A], ec: ExecutionContext): Future[Html] =
    retrievePartial(url"${config.erOutstandingPaymentAmountURL.format(srn)}")

  private def retrievePartial[A](url: URL, extraHeaders: Seq[(String, String)] = Seq.empty)
                                (implicit request: Request[A], ec: ExecutionContext): Future[Html] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierFunctions
        .headerCarrierForPartials(request)
        .toHeaderCarrier
        .withExtraHeaders(extraHeaders*)

    httpClientV2.get(url)
      .setHeader(extraHeaders*)
      .execute[HtmlPartial] recover
            connectionExceptionsAsHtmlPartialFailure map {
      case HtmlPartial.Success(_, content) =>
        content
      case HtmlPartial.Failure(_, _) =>
        logger.warn("Failed to retrieve partial")
        Html("")
    }
  }

}
