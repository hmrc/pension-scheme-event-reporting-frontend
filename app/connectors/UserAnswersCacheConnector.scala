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
import handlers.TaxYearNotAvailableException
import models.UserAnswers
import models.enumeration.EventType
import models.requests.RequiredSchemeDataRequest
import pages.{TaxYearPage, VersionInfoPage}
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

class UserAnswersCacheConnector @Inject()(
                                           config: FrontendAppConfig,
                                           http: HttpClientV2
                                         )(implicit ec: ExecutionContext) extends Logging {

  private def url(srn: String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/user-answers/$srn"
  private def isDataModifiedUrl(srn: String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/compare/$srn"
  private def refreshExpire(srn:String) = url"${config.eventReportingUrl}/pension-scheme-event-reporting/user-answers/refresh-expire/$srn"

  private def noEventHeaders(pstr: String) = Seq(
    ("Content-Type", "application/json"),
    ("pstr", pstr)
  )

  private def eventHeaders(pstr: String, eventType: EventType, noEventJson: Option[JsObject]): Seq[(String, String)] = {
    val headers = noEventJson match {
      case Some(json) =>
        val taxYear = (json \ TaxYearPage.toString).asOpt[String]
        val versionInfo = (json \ VersionInfoPage.toString \ "version").asOpt[Int].map(_.toString)

        Tuple2(taxYear, versionInfo) match {
          case (Some(year), Some(version)) =>
            Seq(
              "Content-Type" -> "application/json",
              "pstr" -> pstr,
              "eventType" -> eventType.toString,
              "year" -> year,
              "version" -> version
            )
          case _ => Nil
        }
      case _ => Nil
    }

    if (headers.isEmpty) {
      throw new TaxYearNotAvailableException("No tax year or version available")
    } else {
      headers
    }
  }

  def getBySrn(pstr: String, eventType: EventType, srn: String)
         (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Option[UserAnswers]] = {
    for {
      noEventData <- getJson(noEventHeaders(pstr), srn)
      eventData <- getJson(eventHeaders(pstr, eventType, noEventData), srn)
    } yield {
      (eventData, noEventData) match {
        case (Some(a), Some(b)) => Some(UserAnswers(data = a, noEventTypeData = b))
        case (None, Some(b)) => Some(UserAnswers(noEventTypeData = b))
        case (Some(a), None) => Some(UserAnswers(data = a))
        case (None, None) => None
      }
    }
  }

  def getByEventType(pstr: String, eventType: EventType)
         (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Option[UserAnswers]] = {
    getBySrn(pstr, eventType, req.srn)
  }

  def postRefreshExpire(implicit req: RequiredSchemeDataRequest[AnyContent], hc: HeaderCarrier): Future[Boolean] = {
    http.post(refreshExpire(req.srn)).execute[HttpResponse].map {
      case resp if resp.status == OK => true
      case _ =>
        logger.warn("Unable to refresh mongo expireAt")
        false
    }
  }

  def isDataModified(pstr: String, eventType: EventType)
         (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Option[Boolean]] = {
    getJson(noEventHeaders(pstr), req.srn).flatMap {
      case Some(noEventData) =>
        val headers = eventHeaders(pstr, eventType, Some(noEventData))
        http.get(isDataModifiedUrl(req.srn)).setHeader(headers*).execute[HttpResponse]
          .map { response =>
            response.status match {
              case NOT_FOUND => None
              case OK => response.json.validate[Boolean] match {
                case JsSuccess(isDataChanged, _) => Some(isDataChanged)
                case JsError(errors) =>
                  logger.warn(s"Unable to de-serialise the response $errors")
                  None
              }
              case _ =>
                None
            }
          }
      case None =>
        Future.successful(None)
    }
  }

  private def getJson(headers: Seq[(String, String)], srn: String)(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier) = {

    http.get(url(srn)).setHeader(headers*).transform(_.withRequestTimeout(config.ifsTimeout)).execute[HttpResponse]
      .recoverWith(mapExceptionsToStatus)
      .map { response =>
        response.status match {
          case NOT_FOUND => None
          case OK => Some(response.json.as[JsObject])
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  def getBySrn(pstr: String, srn: String)(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Option[UserAnswers]] = {
    getJson(noEventHeaders(pstr), srn).map(_.map(d => UserAnswers(noEventTypeData = d)))
  }

  def save(pstr: String, eventType: EventType, userAnswers: JsValue, startYear: String, version: String, srn: String)
          (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "eventType" -> eventType.toString,
      "year" -> startYear,
      "version" -> version
    )

    http.post(url(srn)).withBody(userAnswers).setHeader(headers*).transform(_.withRequestTimeout(config.ifsTimeout)).execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  def save(pstr: String, eventType: EventType, userAnswers: UserAnswers)
          (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Unit] = {

    (userAnswers.get(TaxYearPage), userAnswers.get(VersionInfoPage)) match {
      case (Some(year), Some(version)) =>
        save(pstr, eventType, userAnswers.data, year.startYear, version.version.toString, req.srn)
      case (y, v) =>
        Future.failed(new RuntimeException(s"No tax year or version available: $y / $v"))
    }
  }

  def save(pstr: String, eventType: EventType, userAnswers: UserAnswers, srn: String)
          (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {

    (userAnswers.get(TaxYearPage), userAnswers.get(VersionInfoPage)) match {
      case (Some(year), Some(version)) =>
        save(pstr, eventType, userAnswers.data, year.startYear, version.version.toString, srn)
      case (y, v) =>
        Future.failed(new RuntimeException(s"No tax year or version available: $y / $v"))
    }
  }
  def changeVersion(pstr: String, version: String, newVersion: String)
                   (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Unit] = {
    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "version" -> version,
      "newVersion" -> newVersion
    )

    http.put(url(req.srn)).withBody(Json.obj()).setHeader(headers*).transform(_.withRequestTimeout(config.ifsTimeout)).execute[HttpResponse]
      .map { response =>
        response.status match {
          case NOT_FOUND | NO_CONTENT => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  def saveBySrn(pstr: String, userAnswers: UserAnswers, srn: String)
          (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr
    )

    http.post(url(srn)).withBody(userAnswers.noEventTypeData).setHeader(headers*).transform(_.withRequestTimeout(config.ifsTimeout)).execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  def save(pstr: String, userAnswers: UserAnswers)
          (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Unit] = {
    saveBySrn(pstr, userAnswers, req.srn)
  }

  private def mapExceptionsToStatus: PartialFunction[Throwable, Future[HttpResponse]] = {
    case _: NotFoundException =>
      Future.successful(HttpResponse(NOT_FOUND, "Not found"))
  }

  def removeAll(pstr: String)
               (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Unit] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr
    )

    http.delete(url(req.srn)).setHeader(headers*).transform(_.withRequestTimeout(config.ifsTimeout)).execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }
}

