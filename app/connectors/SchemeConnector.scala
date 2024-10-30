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
import models.{PsaSchemeDetails, PspSchemeDetails}
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http._
import utils.HttpResponseHelper

import java.net.URL
import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class SchemeConnector @Inject()(http: HttpClientV2, config: FrontendAppConfig)
  extends HttpResponseHelper {
  def getOpenDate(idType: String, idValue: String, pstr: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[LocalDate] = {
    val idTypeValue = idType match {
      case "pspId" => Some("pspid")
      case "psaId" => Some("psaid")
      case _ => None
    }

    idTypeValue.map { idTypeValue =>
      val schemeHc = hc.withExtraHeaders("idType" -> idTypeValue, "idValue" -> idValue, "pstr" -> pstr)
      openDate(url"${config.openDateUrl}")(schemeHc, ec)
    }.getOrElse(Future.failed(new IllegalArgumentException("Invalid idType")))
  }
  private def openDate(url: URL)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[LocalDate] = {
    http.get(url).execute[HttpResponse].map { response =>
      response.status match {
        case OK =>
          val openDate = response.body.replace("\"", "")
          LocalDate.parse(openDate)
        case _ =>
          handleErrorResponse("GET", url.toString)(response)
      }
    }
  }
  def getSchemeDetails(psaId: String, idNumber: String, schemeIdType: String)
                      (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[PsaSchemeDetails] = {

    val url = url"${config.schemeDetailsUrl}"

    val headers: Seq[(String, String)] =
      Seq(
        ("idNumber", idNumber),
        ("schemeIdType", schemeIdType),
        ("psaId", psaId)
      )


    http.get(url).setHeader(headers: _*).execute[HttpResponse] map {
      response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[PsaSchemeDetails] match {
              case JsSuccess(value, _) => value
              case JsError(errors) => throw JsResultException(errors)
            }
          case _ =>
            handleErrorResponse("GET", url.toString)(response)
        }
    }
  }
  def getPspSchemeDetails(pspId: String, pstr: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PspSchemeDetails] = {

    val url = url"${config.pspSchemeDetailsUrl}"
    val headers = Seq(("pstr", pstr), ("pspId", pspId))

    http.get(url).setHeader(headers: _*).execute[HttpResponse] map { response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[PspSchemeDetails] match {
              case JsSuccess(value, _) => value
              case JsError(errors) => throw JsResultException(errors)
            }
          case _ =>
            handleErrorResponse("GET", url.toString)(response)
        }
    }
  }
}

