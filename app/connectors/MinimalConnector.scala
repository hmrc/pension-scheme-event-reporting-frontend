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
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HttpClient, _}
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}

class MinimalConnector @Inject()(http: HttpClientV2, config: FrontendAppConfig)
  extends HttpResponseHelper {

  import MinimalConnector._

  def getMinimalDetails(idName: String, idValue: String)
                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MinimalDetails] =
    minDetails(Seq((idName, idValue)))

  private def minDetails(headers: Seq[(String, String)])
                        (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[MinimalDetails] = {

    val url = url"${config.minimalDetailsUrl}"

    http.get(url).setHeader(headers: _*).execute[HttpResponse] map {
      response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[MinimalDetails] match {
              case JsSuccess(value, _) => value
              case JsError(errors) => throw JsResultException(errors)
            }
          case FORBIDDEN if response.body.contains("DELIMITED_PSAID") => throw new DelimitedAdminException
          case _ =>
            handleErrorResponse("GET", url.toString)(response)
        }
    }
  }

}

object MinimalConnector {

  case class MinimalDetails(
                             email: String,
                             isPsaSuspended: Boolean,
                             organisationName: Option[String],
                             individualDetails: Option[IndividualDetails],
                             rlsFlag: Boolean,
                             deceasedFlag: Boolean
                           ) {
    def name: String = (organisationName, individualDetails) match {
      case (Some(orgName), None) => orgName
      case (None, Some(indivName)) => indivName.fullName
      case _ => throw new RuntimeException("Cannot find name")
    }
  }

  object MinimalDetails {
    implicit val format: Format[MinimalDetails] = Json.format[MinimalDetails]
  }

  case class IndividualDetails(firstName: String,
                               middleName: Option[String],
                               lastName: String) {

    def fullName: String = middleName match {
      case Some(middle) => s"$firstName $middle $lastName"
      case _ => s"$firstName $lastName"
    }
  }

  object IndividualDetails {
    implicit val format: Format[IndividualDetails] = Json.format[IndividualDetails]
  }

}

class DelimitedAdminException extends
  Exception("The administrator has already de-registered. The minimal details API has returned a DELIMITED PSA response")