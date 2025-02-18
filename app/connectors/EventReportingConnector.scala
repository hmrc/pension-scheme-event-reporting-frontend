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
import models.amend.VersionsWithSubmitter
import models.requests.{DataRequest, RequiredSchemeDataRequest}
import models.{EROverview, EventDataIdentifier, EventSummary, FileUploadOutcomeResponse, FileUploadOutcomeStatus, UserAnswers}
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.{AnyContent, Result}
import play.api.mvc.Results.{BadRequest, NoContent}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class EventReportingConnector @Inject()(
                                         config: FrontendAppConfig,
                                         httpClientV2: HttpClientV2
                                       )(implicit ec: ExecutionContext) extends HttpResponseHelper {
  private val logger = Logger(classOf[EventReportingConnector])

  private def eventRepSummaryUrl(srn: String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/event-summary/$srn"

  private def eventCompileUrl(srn: String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/compile/$srn"

  private def eventSubmitUrl(srn: String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/submit-event-declaration-report/$srn"

  private def event20ASubmitUrl(srn: String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/submit-event20a-declaration-report/$srn"

  private def getFileUploadResponseUrl(srn: String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/file-upload-response/get/$srn"

  private def eventOverviewUrl(srn: String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/overview/$srn"

  private def erListOfVersionsUrl(srn: String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/versions/$srn"

  private def deleteMemberUrl(srn: String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/delete-member/$srn"


  def getEventReportSummary(pstr: String, reportStartDate: String, version: Int)
                           (implicit headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Seq[EventSummary]] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "reportVersionNumber" -> version.toString,
      "reportStartDate" -> reportStartDate
    )

    httpClientV2.get(eventRepSummaryUrl(req.srn))
      .setHeader(headers: _*)
      .transform(_.withRequestTimeout(config.ifsTimeout))
      .execute[HttpResponse]
      .recoverWith(mapExceptionsToStatus)
      .map { response =>
        response.status match {
          case NOT_FOUND => Nil
          case OK =>
            response.json.as[Seq[EventSummary]]
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  private def mapExceptionsToStatus: PartialFunction[Throwable, Future[HttpResponse]] = {
    case _: NotFoundException =>
      Future.successful(HttpResponse(NOT_FOUND, "Not found"))
  }

  def compileEvent(pstr: String, edi: EventDataIdentifier, currentVersion: Int, delete: Boolean = false)
                  (implicit headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Unit] = {
    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "eventType" -> edi.eventType.toString,
      "year" -> edi.year,
      "currentVersion" -> currentVersion.toString,
      "version" -> edi.version
    ) ++ (if (delete) Seq(("delete", "true")) else Seq())

    httpClientV2.post(eventCompileUrl(req.srn))
      .withBody(Json.obj())
      .setHeader(headers: _*)
      .transform(_.withRequestTimeout(config.ifsTimeout))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case NO_CONTENT => ()
          case NOT_FOUND =>
            logger.error("Compile event returned an unusual http response with status 404 - NOT FOUND")
            throw new HttpException("Not found", response.status)
          case _ =>
            logger.error(s"Compile event return an unusual http response with status ${response.status}")
            throw new HttpException(s"Unexpected status code: ${response.status}", response.status)
        }
    }
  }

  def submitReport(pstr: String, ua: UserAnswers, version: String)
                  (implicit headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Result] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "version" -> version
    )

    httpClientV2.post(eventSubmitUrl(req.srn))
                .withBody(ua.data)
                .transform(_.withRequestTimeout(config.ifsTimeout))
                .setHeader(headers: _*).execute[HttpResponse]
                .map { response =>
                  response.status match {
                    case NO_CONTENT => NoContent
                    case BAD_REQUEST => BadRequest
                    case EXPECTATION_FAILED => throw new ExpectationFailedException("Nothing to submit")
                    case _ =>
                      throw new HttpException(response.body, response.status)
                  }
                }
  }

  def submitReportEvent20A(pstr: String, ua: UserAnswers, version: String)
                          (implicit headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Result] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "version" -> version
    )

    httpClientV2.post(event20ASubmitUrl(req.srn))
      .withBody(ua.data)
      .setHeader(headers: _*)
      .transform(_.withRequestTimeout(config.ifsTimeout))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case NO_CONTENT => NoContent
          case BAD_REQUEST => BadRequest
          case EXPECTATION_FAILED => throw new ExpectationFailedException("Nothing to submit")
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  def getFileUploadOutcome(reference: String)(implicit hc: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[FileUploadOutcomeResponse] = {
    val headers = Seq(("reference", reference))
    httpClientV2.get(getFileUploadResponseUrl(req.srn)).setHeader(headers: _*)
      .transform(_.withRequestTimeout(config.ifsTimeout))
      .execute[HttpResponse]
      .map { response =>
      response.status match {
        case OK =>
          ((response.json \ "fileStatus").asOpt[String],
            (response.json \ "uploadDetails" \ "fileName").asOpt[String],
            (response.json \ "downloadUrl").asOpt[String],
            (response.json \ "uploadDetails" \ "size").asOpt[Long]
          ) match {
            case (Some("READY"), file@Some(_), downloadUrl@Some(_), fileSize@Some(_)) =>
              FileUploadOutcomeResponse(file, FileUploadOutcomeStatus.SUCCESS, downloadUrl, reference, fileSize)
            case _ => FileUploadOutcomeResponse(None, FileUploadOutcomeStatus.FAILURE, None, reference, None)
          }
        case NOT_FOUND => FileUploadOutcomeResponse(None, FileUploadOutcomeStatus.IN_PROGRESS, None, reference, None)
        case _ =>
          throw new HttpException(s"getFileUploadOutcome - unexpected status code: ${response.status}", response.status)
      }

    }
  }

  def getOverview(pstr: String, reportType: String, startDate: String, endDate: String)
                 (implicit headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Seq[EROverview]] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "reportType" -> reportType,
      "startDate" -> startDate,
      "endDate" -> endDate
    )

    httpClientV2
      .get(eventOverviewUrl(req.srn))
      .setHeader(headers: _*)
      .transform(_.withRequestTimeout(config.ifsTimeout))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[Seq[EROverview]](Reads.seq(EROverview.rds)) match {
              case JsSuccess(data, _) => data
              case JsError(errors) => throw JsResultException(errors)
            }
          case _ => throw new HttpException(response.body, response.status)
        }
      }
  }

  def deleteMember(pstr: String, edi: EventDataIdentifier, currentVersion: Int, memberIdToDelete: String)
                  (implicit headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Unit] = {
    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "eventType" -> edi.eventType.toString,
      "year" -> edi.year,
      "version" -> edi.version,
      "currentVersion" -> currentVersion.toString,
      "memberIdToDelete" -> memberIdToDelete
    )

    httpClientV2.post(deleteMemberUrl(req.srn))
      .withBody(Json.obj())
      .setHeader(headers: _*)
      .transform(_.withRequestTimeout(config.ifsTimeout))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case NO_CONTENT => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  def getListOfVersions(pstr: String, startDate: String)(implicit headerCarrier: HeaderCarrier,
                                                         req: RequiredSchemeDataRequest[AnyContent]): Future[Seq[VersionsWithSubmitter]] = {
    val headers = Seq(("pstr", pstr), ("startDate", startDate))
    httpClientV2.get(erListOfVersionsUrl(req.srn))
      .setHeader(headers: _*)
      .transform(_.withRequestTimeout(config.ifsTimeout))
      .execute[HttpResponse]
      .map { response =>
      response.status match {
        case OK =>
          Json.parse(response.body).validate[Seq[VersionsWithSubmitter]] match {
            case JsSuccess(value, _) => value
            case JsError(errors) => throw JsResultException(errors)
          }
        case NOT_FOUND => Seq.empty
        case _ => handleErrorResponse("GET", erListOfVersionsUrl(req.srn).toString)(response)
      }
    } andThen {
      case Failure(t: Throwable) => logger.warn("Unable to get list of versions", t)
    }
  }
}


