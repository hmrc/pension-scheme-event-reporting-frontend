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

import com.google.inject.Inject
import connectors.EventReportingConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.Index
import models.enumeration.EventType.Event23
import models.requests.DataRequest
import pages.event23.Event23CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{ChooseTaxYearSummary, MembersDetailsSummary, TotalPensionAmountsSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event23CheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   connector: EventReportingConnector,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(Event23) andThen requireData) { implicit request =>
      val thisPage = Event23CheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event23.routes.Event23CheckYourAnswersController.onClick.url
      Ok(view(SummaryListViewModel(rows = buildEvent23CYARows(waypoints, thisPage, index)), continueUrl))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event23) andThen requireData).async { implicit request =>
      connector.compileEvent(request.pstr, Event23).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, Event23).url)
      }
    }

  private def buildEvent23CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Int)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage, Event23).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage, Event23).toSeq ++
      ChooseTaxYearSummary.row(request.userAnswers, waypoints, sourcePage, Event23, index).toSeq ++
      TotalPensionAmountsSummary.row(request.userAnswers, waypoints, sourcePage, Event23, index).toSeq
  }
}
