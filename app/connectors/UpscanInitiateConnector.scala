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

import config.FrontendAppConfig
import models.enumeration.EventType
import models.requests.DataRequest
import models.{UpscanFileReference, UpscanInitiateResponse}
import play.api.libs.json._
import play.api.mvc.AnyContent
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

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

class UpscanInitiateConnector @Inject()(httpClient: HttpClient, appConfig: FrontendAppConfig)(implicit ec: ExecutionContext) {

  private val headers = Map(
    HeaderNames.CONTENT_TYPE -> "application/json"
  )

  def initiateV2(redirectOnSuccess: Option[String], redirectOnError: Option[String])
                (implicit request: DataRequest[AnyContent], headerCarrier: HeaderCarrier): Future[UpscanInitiateResponse] = {

    val upscanCallbackURL = s"${appConfig.eventReportingUrl}/pension-scheme-event-reporting/file-upload-response/save"

    val req = UpscanInitiateRequestV2(
      callbackUrl = upscanCallbackURL,
      successRedirect = redirectOnSuccess,
      errorRedirect = redirectOnError,
      maximumFileSize = Some(appConfig.maxUploadFileSize * (1024 * 1024))
    )
    initiate(appConfig.initiateV2Url, req)
  }

  private def initiate[T](url: String, initialRequest: T)(
    implicit request: DataRequest[AnyContent], headerCarrier: HeaderCarrier, wts: Writes[T]): Future[UpscanInitiateResponse] = {

    httpClient.POST[T, PreparedUpload](url, initialRequest, headers.toSeq).map {
      response =>
        val fileReference = UpscanFileReference(response.reference.reference)
        val postTarget = response.uploadRequest.href
        val formFields = response.uploadRequest.fields
        UpscanInitiateResponse(fileReference, postTarget, formFields)
    }
  }

  def download(downloadUrl: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    httpClient.GET[HttpResponse](downloadUrl)(implicitly, hc, implicitly)
  }

  case class UpscanInitiateError(e: Throwable) extends RuntimeException(e)
}