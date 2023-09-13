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

package controllers.testOnly

import com.google.inject.Inject
import forms.testOnly.DateTestFormProvider
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateHelper
import views.html.testOnly.DateTestView

import scala.concurrent.{ExecutionContext, Future}

class DateTestController @Inject()(val controllerComponents: MessagesControllerComponents,
                                   formProvider: DateTestFormProvider,
                                   view: DateTestView
                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = Action.async { implicit request =>
    val preparedForm = DateHelper.overriddenDate.map(formProvider().fill).getOrElse(formProvider())
    Future(Ok(view(preparedForm)))
  }

  def onSubmit(): Action[AnyContent] = Action.async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        value => {
          DateHelper.setDate(Some(value))
          Future.successful(Redirect(controllers.testOnly.routes.DateTestController.onPageLoad().url))
        }
      )
  }
}