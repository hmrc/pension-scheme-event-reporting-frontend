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

import com.google.inject.Inject
import config.FrontendAppConfig
import models.enumeration.AdministratorOrPractitioner
import models.{AdministratorOrPractitioner, JourneyType, SendEmailRequest}
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

sealed trait EmailStatus

case object EmailSent extends EmailStatus

case object EmailNotSent extends EmailStatus

class EmailConnector @Inject()(
                                appConfig: FrontendAppConfig,
                                http: HttpClient,
                                crypto: ApplicationCrypto
                              ) {

  private val logger = Logger(classOf[EmailConnector])

  private def callBackUrl(
    schemeAdministratorType: AdministratorOrPractitioner,
    requestId: String,
    psaOrPspId: String,
    email: String
  ): String = {
    val encryptedPsaOrPspId = crypto.QueryParameterCrypto.encrypt(PlainText(psaOrPspId)).value
    val encryptedEmail = crypto.QueryParameterCrypto.encrypt(PlainText(email)).value

    appConfig.aftEmailCallback(schemeAdministratorType, journeyType, requestId, encryptedEmail, encryptedPsaOrPspId)
  }

  //scalastyle:off parameter.number
  def sendEmail(
                 schemeAdministratorType: AdministratorOrPractitioner = AdministratorOrPractitioner.Administrator,
                 requestId: String,
                 psaOrPspId: String,
                 emailAddress: String,
                 templateParams: Map[String, String]
               )(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[EmailStatus] = {
    val emailServiceUrl = s"${appConfig.emailApiUrl}/hmrc/email"

    val sendEmailReq = SendEmailRequest(List(emailAddress), templateParams, appConfig.emailSendForce,
      callBackUrl(schemeAdministratorType, requestId, psaOrPspId, emailAddress))
    val jsonData = Json.toJson(sendEmailReq)

    http.POST[JsValue, HttpResponse](emailServiceUrl, jsonData).map { response =>
      response.status match {
        case ACCEPTED =>
          logger.debug(s"Email sent successfully for $journeyType")
          EmailSent
        case status =>
          logger.warn(s"Sending Email failed for $journeyType with response status $status")
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
