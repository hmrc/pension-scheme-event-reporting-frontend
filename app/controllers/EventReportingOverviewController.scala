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

import connectors.EventReportingConnector
import controllers.EventReportingOverviewController.OverviewViewModel
import controllers.actions._
import forms.EventSummaryFormProvider
import models.TaxYear.getSelectedTaxYearAsString
import models.{EventSummary, MemberSummaryPath, UserAnswers}
import models.enumeration.EventType
import models.enumeration.EventType.{Event18, Event20A, Event8A, WindUp}
import models.requests.DataRequest
import pages.{EmptyWaypoints, EventSummaryPage, TaxYearPage, VersionInfoPage, Waypoints}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.Message
import views.html.{EventReportingOverviewView, EventSummaryView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class EventReportingOverviewController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         connector: EventReportingConnector,
                                         formProvider: EventSummaryFormProvider,
                                         view: EventReportingOverviewView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>

    val ovm = OverviewViewModel()
      Future.successful(Ok(view(ovm)))
  }




}

object EventReportingOverviewController {
  case class OverviewViewModel(
                                returnUrl: String = "returnUrl",
                                newAftUrl: String = "newAftUrl",
                                paymentsAndChargesUrl: String = "paymentsAndChargesUrl",
                                schemeName: String =  "schemeName",
                                outstandingAmount: String = "outstandingAmount",
                                quartersInProgress: Seq[(String, String)] = Seq(("quartersInProgress1", "quartersInProgress2")),
                                pastYearsAndQuarters: Seq[(Int, Seq[(String, String)])] = Seq((2020, Seq(("pastYearsAndQuarters1", "pastYearsAndQuarters2")))),
                                viewAllPastAftsUrl: String = "viewAllPastAftsUrl"
                              )
}
