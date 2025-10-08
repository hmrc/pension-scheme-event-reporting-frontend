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
import models.SendEmailRequest
import models.enumeration.{AdministratorOrPractitioner, WithName}
import play.api.Logger
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.ApplicationCrypto
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import scala.concurrent.{ExecutionContext, Future}

sealed trait EmailStatus

case object EmailSent extends WithName("EmailSent") with EmailStatus

case object EmailNotSent extends WithName("EmailNotSent") with EmailStatus

class EmailConnector @Inject()(
                                appConfig: FrontendAppConfig,
                                httpClientV2: HttpClientV2,
                                crypto: ApplicationCrypto
                              ) {

  private val logger = Logger(classOf[EmailConnector])

  private def callBackUrl(
                           schemeAdministratorType: AdministratorOrPractitioner,
                           requestId: String,
                           psaOrPspId: String,
                           pstr: String,
                           email: String,
                           reportVersion: String
                         ): String = {
    val encryptedPsaOrPspId = URLEncoder.encode(crypto.QueryParameterCrypto.encrypt(PlainText(psaOrPspId)).value, StandardCharsets.UTF_8.toString)
    val encryptedPstr = URLEncoder.encode(crypto.QueryParameterCrypto.encrypt(PlainText(pstr)).value, StandardCharsets.UTF_8.toString)
    val encryptedEmail = URLEncoder.encode(crypto.QueryParameterCrypto.encrypt(PlainText(email)).value, StandardCharsets.UTF_8.toString)

    appConfig.eventReportingEmailCallback(schemeAdministratorType, requestId, encryptedEmail, encryptedPsaOrPspId, encryptedPstr, reportVersion)
  }

  //scalastyle:off parameter.number
  def sendEmail(
                 schemeAdministratorType: AdministratorOrPractitioner = AdministratorOrPractitioner.Administrator,
                 requestId: String,
                 psaOrPspId: String,
                 pstr: String,
                 emailAddress: String,
                 templateId: String,
                 templateParams: Map[String, String],
                 reportVersion: String
               )(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[EmailStatus] = {
    val emailServiceUrl = url"${appConfig.emailApiUrl}/hmrc/email"

    val sendEmailReq = SendEmailRequest(List(emailAddress), templateId, templateParams, appConfig.emailSendForce,
      callBackUrl(schemeAdministratorType, requestId, psaOrPspId, pstr, emailAddress, reportVersion))
    val jsonData = Json.toJson(sendEmailReq)

    httpClientV2
      .post(emailServiceUrl)
      .withBody(jsonData).execute[HttpResponse].map { response =>
      response.status match {
        case ACCEPTED =>
          logger.debug(s"Email sent successfully")
          EmailSent
        case status =>
          logger.warn(s"Sending Email failed with response status $status")
          EmailNotSent
      }
    } recoverWith logExceptions
  }

  private def logExceptions: PartialFunction[Throwable, Future[EmailStatus]] = {
    case t: Throwable =>
      logger.warn("Unable to connect to Email Service", t)
      Future.successful(EmailNotSent)
  }
}
