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
import models.fileUpload.ParsingAndValidationOutcome
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class ParsingAndValidationOutcomeCacheConnector @Inject()(config: FrontendAppConfig, http: HttpClientV2) {

  private val logger = Logger(classOf[ParsingAndValidationOutcomeCacheConnector])

  private val url = url"${config.parsingAndValidationUrl}"

  def getOutcome(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Option[ParsingAndValidationOutcome]] = {

    val headers: Seq[(String, String)] = Seq(("Content-Type", "application/json"))

    http.get(url).setHeader(headers: _*).execute[HttpResponse]
      .recoverWith(mapExceptionsToStatus)
      .map { response =>
        response.status match {
          case OK =>
            response.json.asOpt[ParsingAndValidationOutcome]
          case _ => None
        }
      }
  }

  def setOutcome(outcome: ParsingAndValidationOutcome)(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {
    val headers: Seq[(String, String)] = Seq(("Content-Type", "application/json"))

    http.post(url)
      .withBody(Json.toJson(outcome))
      .setHeader(headers: _*)
      .execute[HttpResponse] andThen {
      case Failure(t: Throwable) => logger.warn("Unable to post parsing and validation outcome", t)
    } map { _ => () }
  }

  def deleteOutcome(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {
    val headers: Seq[(String, String)] = Seq(("Content-Type", "application/json"))

    http.delete(url).setHeader(headers: _*).execute[HttpResponse] andThen {
      case Failure(t: Throwable) => logger.warn("Unable to delete parsing and validation outcome", t)
    } map { _ => () }
  }

  private def mapExceptionsToStatus: PartialFunction[Throwable, Future[HttpResponse]] = {
    case _: NotFoundException =>
      Future.successful(HttpResponse(NOT_FOUND, "Not found"))
  }
}
