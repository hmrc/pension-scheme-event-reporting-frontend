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

package controllers

import connectors.MinimalConnector
import controllers.actions._
import handlers.TaxYearNotAvailableException
import helpers.DateHelper
import helpers.DateHelper.dateFormatter
import pages.{TaxYearPage, Waypoints}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReturnSubmittedView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReturnSubmittedController @Inject()(
                                           val controllerComponents: MessagesControllerComponents,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           view: ReturnSubmittedView,
                                           minimalConnector: MinimalConnector
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>

    minimalConnector.getMinimalDetails(request.loggedInUser.idName).map { minimalDetails =>
      val schemeName = request.schemeName

      val taxYear = request.userAnswers.get(TaxYearPage) match {
        case Some(taxYear) => s"${taxYear.startYear} ${Messages("confirmation.taxYear.to")} ${taxYear.endYear}"
        case _ => throw new TaxYearNotAvailableException("Tax year not available on Return Submitted Controller")
      }

      val dateHelper = new DateHelper
      val dateSubmitted: String = dateHelper.now.format(dateFormatter)
      val returnUrl = controllers.routes.EventReportingOverviewController.onPageLoad(request.srn).url

      Ok(view(controllers.routes.ReturnSubmittedController.onPageLoad(waypoints).url, schemeName, taxYear, dateSubmitted, minimalDetails.email, returnUrl))
    }
  }
}
