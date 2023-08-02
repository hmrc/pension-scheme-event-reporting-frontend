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
import models.SchemeDetails
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HttpClient, _}
import utils.HttpResponseHelper

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class SchemeConnector @Inject()(http: HttpClient, config: FrontendAppConfig)
  extends HttpResponseHelper {
  def getOpenDate(idType: String, idValue: String, pstr: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[LocalDate] = {
    if (idType == "pspId") {
      val schemeHc = hc.withExtraHeaders("idType" -> "pspId", "idValue" -> idValue, "pstr" -> pstr)
      openDate(config.openDateUrl)(schemeHc, ec)
    } else if (idType == "psaId") {
      val schemeHc = hc.withExtraHeaders("idType" -> "psaId", "idValue" -> idValue, "pstr" -> pstr)
      openDate(config.openDateUrl)(schemeHc, ec)
    } else {
      Future.failed(new IllegalArgumentException("Invalid idType"))
    }
  }
  private def openDate(url: String)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[LocalDate] = {
    http.GET[HttpResponse](url).map { response =>
      response.status match {
        case OK =>
          val openDate = response.body
          val y = LocalDate.parse(openDate)
          println(s"\n\n\n PARSED OPEN DATE CONNECTOR === ${y}")
          y
        case _ =>
          println(s"\n\n\nFAILURE HERE\n\n")
          handleErrorResponse("GET", url)(response)
      }
    }
  }
  def getSchemeDetails(psaId: String, idNumber: String, schemeIdType: String)
                      (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[SchemeDetails] = {

    val url = config.schemeDetailsUrl

    val headers: Seq[(String, String)] =
      Seq(
        ("idNumber", idNumber),
        ("schemeIdType", schemeIdType),
        ("psaId", psaId)
      )

    implicit val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.GET[HttpResponse](url)(implicitly, hc, implicitly) map {
      response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[SchemeDetails](SchemeDetails.readsPsa) match {
              case JsSuccess(value, _) => value
              case JsError(errors) => throw JsResultException(errors)
            }
          case _ =>
            handleErrorResponse("GET", url)(response)
        }
    }
  }
  def getPspSchemeDetails(pspId: String, pstr: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeDetails] = {

    val url = config.pspSchemeDetailsUrl
    val schemeHc = hc.withExtraHeaders("pstr" -> pstr, "pspId" -> pspId)

    http.GET[HttpResponse](url)(implicitly, schemeHc, implicitly) map { response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[SchemeDetails](SchemeDetails.readsPsp) match {
              case JsSuccess(value, _) => value
              case JsError(errors) => throw JsResultException(errors)
            }
          case _ =>
            handleErrorResponse("GET", url)(response)
        }
    }
  }
}

