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

import audit.{AuditService, EventReportingUpscanFileUploadAuditEvent}
import config.FrontendAppConfig
import models.enumeration.EventType
import models.requests.DataRequest
import models.{UpscanFileReference, UpscanInitiateResponse}
import org.apache.pekko.util.ByteString
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.{BodyWritable, InMemoryBody}
import play.api.mvc.AnyContent
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure
import scala.reflect.runtime.universe._
import izumi.reflect.Tag

sealed trait UpscanInitiateRequest

object MaximumFileSize {
  val size = 512
}

case class UpscanInitiateRequestV2(
                                    callbackUrl: String,
                                    successRedirect: Option[String] = None,
                                    errorRedirect: Option[String] = None,
                                    minimumFileSize: Option[Int] = None,
                                    maximumFileSize: Option[Int] = Some(MaximumFileSize.size),
                                    expectedContentType: Option[String] = None)
  extends UpscanInitiateRequest

case class UploadForm(href: String, fields: Map[String, String])

case class Reference(reference: String) extends AnyVal

object Reference {
  implicit val referenceReader: Reads[Reference] = Reads.StringReads.map(Reference(_))
  implicit val referenceWrites: OWrites[Reference] = Json.writes[Reference]
}

case class PreparedUpload(reference: Reference, uploadRequest: UploadForm)

object UpscanInitiateRequestV2 {
  implicit val format: OFormat[UpscanInitiateRequestV2] = Json.format[UpscanInitiateRequestV2]
}

object PreparedUpload {

  implicit val uploadFormFormat: Reads[UploadForm] = Json.reads[UploadForm]

  implicit val format: Reads[PreparedUpload] = Json.reads[PreparedUpload]
}

class UpscanInitiateConnector @Inject()(httpClientV2: HttpClientV2, appConfig: FrontendAppConfig,
                                        auditService: AuditService)(implicit ec: ExecutionContext) {

  private val headers = Seq(
    (HeaderNames.CONTENT_TYPE, "application/json")
  )
  private val logger = Logger(classOf[UpscanInitiateConnector])

  def initiateV2(redirectOnSuccess: Option[String], redirectOnError: Option[String], eventType: EventType)
                (implicit request: DataRequest[AnyContent], headerCarrier: HeaderCarrier): Future[UpscanInitiateResponse] = {

    val upscanCallbackURL = s"${appConfig.eventReportingUrl}/pension-scheme-event-reporting/file-upload-response/save"
    logger.info(s"Upscan initiation: callback URL is $upscanCallbackURL")
    val req = UpscanInitiateRequestV2(
      callbackUrl = upscanCallbackURL,
      successRedirect = redirectOnSuccess,
      errorRedirect = redirectOnError,
      maximumFileSize = Some(appConfig.maxUploadFileSize * (1024 * 1024))
    )
    initiate(url"${appConfig.initiateV2Url}", req, eventType)
  }

  private def initiate[T: Tag](url: URL, initialRequest: T, eventType: EventType)(
    implicit request: DataRequest[AnyContent], headerCarrier: HeaderCarrier, wts: Writes[T]): Future[UpscanInitiateResponse] = {
    // Define an implicit BodyWritable for any type T that has a Writes[T]
    implicit def jsonBodyWritable[T](implicit writes: Writes[T]): BodyWritable[T] = {
      BodyWritable(a => InMemoryBody(ByteString.fromString(Json.stringify(Json.toJson(a)))), "application/json")
    }
    val startTime = System.currentTimeMillis
    httpClientV2.post(url)
      .withBody(initialRequest)
      .setHeader(headers: _*)
      .execute[PreparedUpload]
      .map {
      response =>
        val fileReference = UpscanFileReference(response.reference.reference)
        val postTarget = response.uploadRequest.href
        val formFields = response.uploadRequest.fields
        UpscanInitiateResponse(fileReference, postTarget, formFields)
    } andThen {
      case Failure(t) =>
        sendFailureAuditEvent(eventType, t.getMessage, startTime)
    }
  }

  private def sendFailureAuditEvent(
                                     eventType: EventType,
                                     errorMessage: String,
                                     startTime: Long)(implicit request: DataRequest[AnyContent]): Unit = {

    val endTime = System.currentTimeMillis
    val duration = endTime - startTime

    auditService.sendEvent(
      EventReportingUpscanFileUploadAuditEvent(
        eventType = eventType,
        psaOrPspId = request.loggedInUser.psaIdOrPspId,
        pstr = request.pstr,
        schemeAdministratorType = request.loggedInUser.administratorOrPractitioner,
        outcome = Left(errorMessage),
        uploadTimeInMilliSeconds = duration
      )
    )
  }
  def download(downloadUrl: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    httpClientV2.get(new URL(downloadUrl)).stream
  }

  case class UpscanInitiateError(e: Throwable) extends RuntimeException(e)
}