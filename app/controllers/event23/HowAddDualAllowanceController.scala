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

package controllers.event23

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event23.HowAddDualAllowanceFormProvider
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.Waypoints
import pages.event23.HowAddDualAllowancePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event23.HowAddDualAllowanceView

import javax.inject.Inject

class HowAddDualAllowanceController @Inject()(val controllerComponents: MessagesControllerComponents,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              formProvider: HowAddDualAllowanceFormProvider,
                                              view: HowAddDualAllowanceView
                                             ) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event23

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    Ok(view(form, waypoints, index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, waypoints, index)),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(HowAddDualAllowancePage(index), value)
          Redirect(HowAddDualAllowancePage(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      )
  }

}
