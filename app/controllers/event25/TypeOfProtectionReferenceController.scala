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

package controllers.event25

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event25.TypeOfProtectionReferenceFormProvider
import models.{Index, UserAnswers}
import pages.Waypoints
import pages.event25.TypeOfProtectionReferencePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event25.TypeOfProtectionReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeOfProtectionReferenceController @Inject()(val controllerComponents: MessagesControllerComponents,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    userAnswersCacheConnector: UserAnswersCacheConnector,
                                                    formProvider: TypeOfProtectionReferenceFormProvider,
                                                    view: TypeOfProtectionReferenceView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData()) { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(TypeOfProtectionReferencePage(index))).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints, index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData()).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints, index))),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(TypeOfProtectionReferencePage(index), value)
          userAnswersCacheConnector.save(request.pstr, updatedAnswers).map { _ =>
            Redirect(TypeOfProtectionReferencePage(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }
}