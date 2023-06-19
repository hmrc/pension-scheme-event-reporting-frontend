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

package controllers

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.WantToSubmitFormProvider
import models.UserAnswers
import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import pages.{DeclarationPage, WantToSubmitPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.WantToSubmitView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WantToSubmitController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        formProvider: WantToSubmitFormProvider,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        view: WantToSubmitView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()) { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(WantToSubmitPage)).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, waypoints))),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(WantToSubmitPage, value)
          userAnswersCacheConnector.save(request.pstr, updatedAnswers).map { _ =>
            if (value) {
              Redirect(
                request.loggedInUser.administratorOrPractitioner match {
                  case Administrator => routes.DeclarationController.onPageLoadAdministrator(waypoints)
                  case Practitioner => routes.DeclarationController.onPageLoadPractitioner(waypoints)
                }
              )
            } else {
              Redirect(request.returnUrl)
            }
          }
        }
      )
  }
}