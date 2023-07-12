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

import config.FrontendAppConfig
import connectors.UpscanInitiateConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType
import models.enumeration.EventType.getEventTypeByName
import models.requests.DataRequest
import pages.Waypoints
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fileUpload.FileUploadView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FileUploadController @Inject()(val controllerComponents: MessagesControllerComponents,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     upscanInitiateConnector: UpscanInitiateConnector,
                                     appConfig: FrontendAppConfig,
                                     view: FileUploadView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)
    andThen requireData).async { implicit request =>

    val successRedirectUrl = appConfig.successEndPointTarget(eventType)
    val errorRedirectUrl = appConfig.failureEndPointTarget(eventType)
    upscanInitiateConnector.initiateV2(Some(successRedirectUrl), Some(errorRedirectUrl), eventType).map { uir =>
      Ok(view(waypoints, getEventTypeByName(eventType), eventType, Call("post", uir.postTarget), uir.formFields, getErrorCode(request)))
    }
  }

  private def getErrorCode(request: DataRequest[AnyContent])(implicit messages: Messages): Option[ErrorMessage] = {
    if (request.queryString.contains("errorCode") && request.queryString("errorCode").nonEmpty) {
      request.queryString("errorCode").headOption.map { error =>
        ErrorMessage(content = Text(messages("fileUpload.error.rejected." + error, appConfig.maxUploadFileSize)))
      }
    } else {
      None
    }
  }
}
