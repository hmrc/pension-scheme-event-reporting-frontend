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
import models.amend.VersionsWithSubmitter
import models.{EROverview, EventDataIdentifier, EventSummary, FileUploadOutcomeResponse, FileUploadOutcomeStatus, ToggleDetails, UserAnswers}
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, NoContent}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class EventReportingConnector @Inject()(
                                         config: FrontendAppConfig,
                                         http: HttpClient
                                       )(implicit ec: ExecutionContext) extends HttpResponseHelper {
  private val logger = Logger(classOf[EventReportingConnector])

  private def eventRepSummaryUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/event-summary"

  private def eventCompileUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/compile"

  private def eventSubmitUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/submit-event-declaration-report"

  private def event20ASubmitUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/submit-event20a-declaration-report"

  private def getFileUploadResponseUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/file-upload-response/get"

  private def eventReportingToggleUrl(toggleName: String) = s"${config.eventReportingUrl}/admin/get-toggle/$toggleName"

  private def eventOverviewUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/overview"

  private def erListOfVersionsUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/versions"

  private def deleteMemberUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/delete-member"


  def getEventReportSummary(pstr: String, reportStartDate: String, version: Int)
                           (implicit headerCarrier: HeaderCarrier): Future[Seq[EventSummary]] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "reportVersionNumber" -> version.toString,
      "reportStartDate" -> reportStartDate
    )
    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.GET[HttpResponse](eventRepSummaryUrl)(implicitly, hc, implicitly)
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
                  (implicit headerCarrier: HeaderCarrier): Future[Unit] = {
    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "eventType" -> edi.eventType.toString,
      "year" -> edi.year,
      "currentVersion" -> currentVersion.toString,
      "version" -> edi.version
    ) ++ (if (delete) Seq(("delete", "true")) else Seq())
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

  def submitReport(pstr: String, ua: UserAnswers, version: String)
                  (implicit headerCarrier: HeaderCarrier): Future[Result] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "version" -> version
    )

    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.POST[JsValue, HttpResponse](eventSubmitUrl, ua.data)(implicitly, implicitly, hc, implicitly)
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
                          (implicit headerCarrier: HeaderCarrier): Future[Result] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "version" -> version
    )

    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.POST[JsValue, HttpResponse](event20ASubmitUrl, ua.data)(implicitly, implicitly, hc, implicitly)
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

  def getFeatureToggle(toggleName: String)(implicit hc: HeaderCarrier): Future[ToggleDetails] = {
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

  def getFileUploadOutcome(reference: String)(implicit hc: HeaderCarrier): Future[FileUploadOutcomeResponse] = {
    val headerCarrier: HeaderCarrier = hc.withExtraHeaders("reference" -> reference)
    http.GET[HttpResponse](getFileUploadResponseUrl)(implicitly, headerCarrier, implicitly).map { response =>
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
          throw new HttpException(response.body, response.status)
      }

    }
  }

  def getOverview(pstr: String, reportType: String, startDate: String, endDate: String)
                 (implicit headerCarrier: HeaderCarrier): Future[Seq[EROverview]] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "reportType" -> reportType,
      "startDate" -> startDate,
      "endDate" -> endDate
    )
    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.GET[HttpResponse](eventOverviewUrl)(implicitly, hc, implicitly)
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
                  (implicit headerCarrier: HeaderCarrier): Future[Unit] = {
    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "eventType" -> edi.eventType.toString,
      "year" -> edi.year,
      "version" -> edi.version,
      "currentVersion" -> currentVersion.toString,
      "memberIdToDelete" -> memberIdToDelete
    )
    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.POST[JsValue, HttpResponse](deleteMemberUrl, Json.obj())(implicitly, implicitly, hc, implicitly)
      .map { response =>
        response.status match {
          case NO_CONTENT => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  def getListOfVersions(pstr: String, startDate: String)(implicit headerCarrier: HeaderCarrier): Future[Seq[VersionsWithSubmitter]] = {
    val hc = headerCarrier.withExtraHeaders("pstr" -> pstr, "startDate" -> startDate)
    http.GET[HttpResponse](erListOfVersionsUrl)(implicitly, hc, implicitly).map { response =>
      response.status match {
        case OK =>
          Json.parse(response.body).validate[Seq[VersionsWithSubmitter]] match {
            case JsSuccess(value, _) => value
            case JsError(errors) => throw JsResultException(errors)
          }
        case NOT_FOUND => Seq.empty
        case _ => handleErrorResponse("GET", erListOfVersionsUrl)(response)
      }
    } andThen {
      case Failure(t: Throwable) => logger.warn("Unable to get list of versions", t)
    }
  }
}


