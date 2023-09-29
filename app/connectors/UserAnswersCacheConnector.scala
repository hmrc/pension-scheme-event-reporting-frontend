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
import models.UserAnswers
import models.enumeration.EventType
import pages.{TaxYearPage, VersionInfoPage}
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}

class UserAnswersCacheConnector @Inject()(
                                           config: FrontendAppConfig,
                                           http: HttpClient
                                         ) {

  private def url = s"${config.eventReportingUrl}/pension-scheme-event-reporting/user-answers"

  private def noEventHeaders(pstr: String) = Seq(
    "Content-Type" -> "application/json",
    "pstr" -> pstr
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
      throw new RuntimeException("No tax year or version available")
    } else {
      headers
    }
  }

  def get(pstr: String, eventType: EventType)
         (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Option[UserAnswers]] = {
    for {
      noEventData <- getJson(noEventHeaders(pstr))
      eventData <- getJson(eventHeaders(pstr, eventType, noEventData))
    } yield {
      (eventData, noEventData) match {
        case (Some(a), Some(b)) => Some(UserAnswers(data = a, noEventTypeData = b))
        case (None, Some(b)) => Some(UserAnswers(noEventTypeData = b))
        case (Some(a), None) => Some(UserAnswers(data = a))
        case (None, None) => None
      }
    }
  }

  private def getJson(headers: Seq[(String, String)])(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier) = {
    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.GET[HttpResponse](url)(implicitly, hc, implicitly)
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

  def get(pstr: String)(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Option[UserAnswers]] = {
    getJson(noEventHeaders(pstr)).map(_.map(d => UserAnswers(noEventTypeData = d)))
  }

  def save(pstr: String, eventType: EventType, userAnswers: UserAnswers)
          (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {

    (userAnswers.get(TaxYearPage), userAnswers.get(VersionInfoPage)) match {
      case (Some(year), Some(version)) =>
        val headers: Seq[(String, String)] = Seq(
          "Content-Type" -> "application/json",
          "pstr" -> pstr,
          "eventType" -> eventType.toString,
          "year" -> year.startYear,
          "version" -> version.version.toString
        )

        val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

        http.POST[JsValue, HttpResponse](url, userAnswers.data)(implicitly, implicitly, hc, implicitly)
          .map { response =>
            response.status match {
              case OK => ()
              case _ =>
                throw new HttpException(response.body, response.status)
            }
          }
      case (y, v) =>
        Future.failed(new RuntimeException(s"No tax year or version available: $y / $v"))
    }
  }

  def changeVersion(pstr: String, version: String, newVersion: String)
                   (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {
    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr,
      "version" -> version,
      "newVersion" -> newVersion
    )

    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.PUT[JsValue, HttpResponse](url, Json.obj())(implicitly, implicitly, hc, implicitly)
      .map { response =>
        response.status match {
          case NOT_FOUND | NO_CONTENT => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  def save(pstr: String, userAnswers: UserAnswers)
          (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr
    )

    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)
    http.POST[JsValue, HttpResponse](url, userAnswers.noEventTypeData)(implicitly, implicitly, hc, implicitly)
      .map { response =>
        response.status match {
          case OK => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }

  private def mapExceptionsToStatus: PartialFunction[Throwable, Future[HttpResponse]] = {
    case _: NotFoundException =>
      Future.successful(HttpResponse(NOT_FOUND, "Not found"))
  }

  def removeAll(pstr: String)
               (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {

    val headers: Seq[(String, String)] = Seq(
      "Content-Type" -> "application/json",
      "pstr" -> pstr
    )

    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.DELETE[HttpResponse](url)(implicitly, hc, implicitly)
      .map { response =>
        response.status match {
          case OK => ()
          case _ =>
            throw new HttpException(response.body, response.status)
        }
      }
  }
}


