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

package controllers.fileUpload

import connectors.{EventReportingConnector, UpscanInitiateConnector}
import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IndexView
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import fileUploadParser.CSVParser
import pages.Waypoints
import play.api.mvc.Request

import scala.concurrent.Future

class ValidationController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 identify: IdentifierAction,
                                 eventReportingConnector: EventReportingConnector,
                                 upscanInitiateConnector: UpscanInitiateConnector,
                                 view: IndexView
                               )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = identify { implicit request =>
    getUpscanFileAndParse
    Ok(view())
  }

  private def getUpscanFileAndParse(implicit request: Request[AnyContent]): Future[Seq[Array[String]]] = {
    request.queryString.get("key")
    val referenceOpt: Option[String] = request.queryString.get("key").flatMap { values =>
      values.headOption
    }

    referenceOpt match {
      case Some(reference) => {
        eventReportingConnector.getFileUploadOutcome(reference).flatMap { fileUploadOutcomeResponse =>
          fileUploadOutcomeResponse.downloadUrl match {
            case Some(downloadUrl) => {
              upscanInitiateConnector.download(downloadUrl).map { httpResponse =>
                httpResponse.status match {
                  case OK => CSVParser.split(httpResponse.body)
                  case _ => throw new RuntimeException("Unhandled response from upscan in FileUploadResultController")
                }
              }
            }
            case None => throw new RuntimeException("No download url in FileUploadResultController")
          }
        }
      }
      case _ => throw new RuntimeException("No reference number in FileUploadResultController")
    }
  }
}
