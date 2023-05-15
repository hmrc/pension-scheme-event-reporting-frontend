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

package controllers.event11

import com.google.inject.Inject
import connectors.EventReportingConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType.Event11
import models.requests.DataRequest
import pages.event11.{Event11CheckYourAnswersPage, HasSchemeChangedRulesInvestmentsInAssetsPage, HasSchemeChangedRulesPage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.event11.checkAnswers.{HasSchemeChangedRulesInvestmentsInAssetsSummary, HasSchemeChangedRulesSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class Event11CheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   connector: EventReportingConnector,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData(Event11) andThen requireData) { implicit request =>
      val thisPage = Event11CheckYourAnswersPage()
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event11.routes.Event11CheckYourAnswersController.onClick.url
      Ok(view(SummaryListViewModel(rows = buildEvent11CYARows(waypoints, thisPage)), continueUrl))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event11) andThen requireData).async { implicit request =>
      // If answered "No" twice, you cannot submit this event.
      val maybeNo1 = request.userAnswers.get(HasSchemeChangedRulesPage).getOrElse(false)
      val maybeNo2 = request.userAnswers.get(HasSchemeChangedRulesInvestmentsInAssetsPage).getOrElse(false)
      (maybeNo1, maybeNo2) match {
        case (false, false) =>
          Future.successful(Redirect(controllers.event11.routes.Event11CannotSubmitController.onPageLoad(EmptyWaypoints).url))
        case _ =>
          connector.compileEvent(request.pstr, Event11).map {
            _ =>
              Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
          }
      }
    }

  private def buildEvent11CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    Seq(
      HasSchemeChangedRulesSummary.row(request.userAnswers, waypoints, sourcePage) ++
        HasSchemeChangedRulesInvestmentsInAssetsSummary.row(request.userAnswers, waypoints, sourcePage)
    ).flatten
  }
}
