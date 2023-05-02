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
import connectors.{UpscanInitiateConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.fileUpload.FileUploadFormProvider
import models.{UploadId, UserAnswers}
import models.enumeration.EventType
import models.enumeration.EventType.getEventTypeByName
import pages.Waypoints
import pages.fileUpload.FileUploadPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fileUpload.FileUploadView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileUploadController @Inject()(val controllerComponents: MessagesControllerComponents,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       userAnswersCacheConnector: UserAnswersCacheConnector,
                                       upscanInitiateConnector: UpscanInitiateConnector,
                                       formProvider: FileUploadFormProvider,
                                       appConfig: FrontendAppConfig,
                                       view: FileUploadView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)
    andThen requireData).async { implicit request =>
    val uploadId = UploadId.generate
    val successRedirectUrl = appConfig.successEndPointTarget(eventType,uploadId)
    val errorRedirectUrl = appConfig.failureEndPointTarget(eventType)

    upscanInitiateConnector.initiateV2(Some(successRedirectUrl), Some(errorRedirectUrl), eventType).map { uir =>
      val preparedForm = request.userAnswers.get(FileUploadPage(eventType)).fold(form)(form.fill)
      Ok(view(preparedForm, waypoints, getEventTypeByName(eventType), eventType, Call("post", uir.postTarget), uir.formFields))
    }
  }

//  def onSubmit(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)).async {
//    implicit request =>
//      form.bindFromRequest().fold(
//        formWithErrors =>
//          Future.successful(BadRequest(view(formWithErrors, waypoints, getEventTypeByName(eventType), eventType,
//            controllers.fileUpload.routes.FileUploadResultController.onPageLoad(waypoints)))),
//        value => {
//          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
//          val updatedAnswers = originalUserAnswers.setOrException(FileUploadPage(eventType), value)
//          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
//            Redirect(FileUploadPage(eventType).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
//          }
//        }
//      )
//  }
}