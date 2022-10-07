/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}

class EventReportingConnector @Inject()(
                                           config: FrontendAppConfig,
                                           http: HttpClient
                                         ) {

  private def eventRepSummaryUrl = s"${config.eventReportingUrl}/pension-scheme-event-reporting/event-summary"

  def getEventReportSummary(pstr: String)
         (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Seq[EventType]] = {

    Future.successful(Seq(EventType.Event1, EventType.Event3))
//    val headers: Seq[(String, String)] = Seq(
//      "Content-Type" -> "application/json",
//      "pstr" -> pstr,
//      "reportVersionNumber" -> "001",
//      "reportStartDate" -> "21/01/22"
//    )
//    val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)
//
//    http.GET[HttpResponse](eventRepSummaryUrl)(implicitly, hc, implicitly)
//      .recoverWith(mapExceptionsToStatus)
//      .map { response =>
//        response.status match {
//          case NOT_FOUND => Nil
//          case OK => response.json.as[Seq[EventType]]
//          case _ =>
//            throw new HttpException(response.body, response.status)
//        }
//      }
  }

  private def mapExceptionsToStatus: PartialFunction[Throwable, Future[HttpResponse]] = {
    case _: NotFoundException =>
      Future.successful(HttpResponse(NOT_FOUND, "Not found"))
  }
}


