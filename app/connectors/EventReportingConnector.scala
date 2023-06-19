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
import models.enumeration.EventType
import models.{EventDataIdentifier, FileUploadOutcomeResponse, FileUploadOutcomeStatus, ToggleDetails, UserAnswers}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}

class EventReportingConnector @Inject()(
                                         config: FrontendAppConfig,
                                         http: HttpClient
                                       ) {

  private def eventRepSummaryUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/event-summary"
  private def eventCompileUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/compile"
  private def eventSubmitUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/submit-event-declaration-report"
  private def getFileUploadResponseUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/file-upload-response/get"

  private def eventReportingToggleUrl(toggleName:String) = s"${config.eventReportingUrl}/admin/get-toggle/$toggleName"

  def getEventReportSummary(pstr: String, reportStartDate: String)
                           (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Seq[EventType]] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "reportVersionNumber" -> "001",
      "reportStartDate" -> reportStartDate
    )
    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.GET[HttpResponse](eventRepSummaryUrl)(implicitly, hc, implicitly)
      .recoverWith(mapExceptionsToStatus)
      .map { response =>
        response.status match {
          case NOT_FOUND => Nil
          case OK =>
            response.json.as[Seq[EventType]]
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  private def mapExceptionsToStatus: PartialFunction[Throwable, Future[HttpResponse]] = {
    case _: NotFoundException =>
      Future.successful(HttpResponse(NOT_FOUND, "Not found"))
  }

  def compileEvent(pstr: String, edi: EventDataIdentifier)
                  (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {
    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "eventType" -> edi.eventType.toString,
      "year" -> edi.year,
      "version" -> edi.version
    )
    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.POST[JsValue, HttpResponse](eventCompileUrl, Json.obj())(implicitly, implicitly, hc, implicitly)
      .map { response =>
        response.status match {
          case NO_CONTENT => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  def submitReport(pstr: String, ua: UserAnswers)
                  (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr
    )

    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.POST[JsValue, HttpResponse](eventSubmitUrl, ua.data)(implicitly, implicitly, hc, implicitly)
      .map { response =>
        response.status match {
          case NO_CONTENT => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  def getFeatureToggle(toggleName: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ToggleDetails] = {
    http.GET[HttpResponse](eventReportingToggleUrl(toggleName))(implicitly, hc, implicitly).map { response =>
      val toggleOpt = response.status match {
        case NO_CONTENT => None
        case OK =>
          Some(response.json.as[ToggleDetails])
        case _ =>
          throw new HttpException(response.body, response.status)
      }

      toggleOpt match {
        case None => ToggleDetails(toggleName, None, isEnabled = false)
        case Some(a) => a
      }
    }
  }

  def getFileUploadOutcome(reference: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[FileUploadOutcomeResponse] = {
    val headerCarrier: HeaderCarrier = hc.withExtraHeaders("reference" -> reference)
    http.GET[HttpResponse](getFileUploadResponseUrl)(implicitly, headerCarrier, implicitly).map{ response =>
      response.status match {
        case OK =>
          ((response.json \ "fileStatus").asOpt[String],
            (response.json \ "uploadDetails" \ "fileName").asOpt[String],
            (response.json \ "downloadUrl").asOpt[String]) match {
            case (Some("READY"),file@Some(_), downloadUrl@Some(_)) =>
              FileUploadOutcomeResponse(file, FileUploadOutcomeStatus.SUCCESS, downloadUrl)
            case _ => FileUploadOutcomeResponse(None, FileUploadOutcomeStatus.FAILURE, None)
          }
        case NOT_FOUND => FileUploadOutcomeResponse(None, FileUploadOutcomeStatus.IN_PROGRESS, None)
        case _ =>
          throw new HttpException(response.body, response.status)
      }

    }
  }
}


