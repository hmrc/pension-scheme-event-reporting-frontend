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

import config.FrontendAppConfig
import controllers.actions._
import helpers.DateHelper
import helpers.DateHelper.dateFormatter
import pages.{TaxYearPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.i18n.Lang.logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReturnSubmittedView

import javax.inject.Inject

class ReturnSubmittedController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        view: ReturnSubmittedView,
                                        config: FrontendAppConfig
                                      ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData)  { implicit request =>
    val schemeName: String = request.schemeName

    request.userAnswers.get(TaxYearPage) match {
      case Some(taxYear) => taxYear
      case _ => logger.error("No tax year on return submitted page")
      //ToDo - How to handle this case? Refactor all this to format better
    }

    val dateHelper = new DateHelper
    val dateSubmitted: String = dateHelper.now.format(dateFormatter)

      Ok(view(controllers.routes.ReturnSubmittedController.onPageLoad(waypoints).url, config.yourPensionSchemesUrl, schemeName, dateSubmitted))
  }
}
