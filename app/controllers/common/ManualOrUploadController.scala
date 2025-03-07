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

package controllers.common

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.common.ManualOrUploadFormProvider
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.Waypoints
import pages.common.ManualOrUploadPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.common.ManualOrUploadView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ManualOrUploadController @Inject()(val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         formProvider: ManualOrUploadFormProvider,
                                         view: ManualOrUploadView,
                                         userAnswersCacheConnector: UserAnswersCacheConnector
                                        ) (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    val form = formProvider(eventType)
    val preparedForm = request.userAnswers.flatMap(_.get(ManualOrUploadPage(eventType, index))).fold(form){v => form.fill(v)}
    Ok(view(preparedForm, waypoints, eventType, index))
  }

  def onSubmit(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) {
    implicit request =>
      val form = formProvider(eventType)
      form.bindFromRequest().fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, waypoints, eventType, index)),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(ManualOrUploadPage(eventType, index), value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers)
          Redirect(ManualOrUploadPage(eventType, index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      )
  }

}
