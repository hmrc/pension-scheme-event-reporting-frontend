/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.event22

import com.google.inject.Inject
import connectors.EventReportingConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType.Event22
import models.requests.DataRequest
import pages.event22.Event22CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{ChooseTaxYearSummary, MembersDetailsSummary, TotalPensionAmountsSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event22CheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   connector: EventReportingConnector,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData(Event22) andThen requireData) { implicit request =>
      val thisPage = Event22CheckYourAnswersPage
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event22.routes.Event22CheckYourAnswersController.onClick.url
      Ok(view(SummaryListViewModel(rows = buildEvent22CYARows(waypoints, thisPage)), continueUrl))
    }

  /**
   * TODO: replace controllers.event22.routes.Event22CheckYourAnswersController with actual event22.SummaryPageController
   */
  def onClick: Action[AnyContent] =
    (identify andThen getData(Event22) andThen requireData).async { implicit request =>
      connector.compileEvent("123", Event22).map {
        _ =>
          Redirect(controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad().url)
      }
    }

  private def buildEvent22CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, None, sourcePage, Event22).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, None, sourcePage, Event22).toSeq ++
      ChooseTaxYearSummary.row(request.userAnswers, waypoints, sourcePage, Event22).toSeq ++
      TotalPensionAmountsSummary.row(request.userAnswers, waypoints, sourcePage, Event22).toSeq
  }
}
