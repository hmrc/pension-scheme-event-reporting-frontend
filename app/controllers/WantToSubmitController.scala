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

import controllers.actions._
import forms.WantToSubmitFormProvider
import models.UserAnswers
import pages.{WantToSubmitPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.WantToSubmitView

import javax.inject.Inject

class WantToSubmitController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        formProvider: WantToSubmitFormProvider,
                                        view: WantToSubmitView
                                      ) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = identify { implicit request =>
    Ok(view(form, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = identify {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => BadRequest(view(formWithErrors, waypoints)),
        value => {
          val ua: UserAnswers = UserAnswers().setOrException(WantToSubmitPage(), value)
          Redirect(WantToSubmitPage().navigate(waypoints, ua, ua).route)
        }
      )
  }
}
