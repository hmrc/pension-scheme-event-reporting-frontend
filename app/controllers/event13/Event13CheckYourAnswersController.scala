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

package controllers.event13

import com.google.inject.Inject
import connectors.EventReportingConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType.Event13
import models.requests.DataRequest
import pages.event13.Event13CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{ChangeDateSummary, SchemeStructureDescriptionSummary}
import viewmodels.event13.checkAnswers.SchemeStructureSummary
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event13CheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   connector: EventReportingConnector,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData(Event13) andThen requireData) { implicit request =>
      val thisPage = Event13CheckYourAnswersPage()
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event13.routes.Event13CheckYourAnswersController.onClick.url
      Ok(view(SummaryListViewModel(rows = buildEvent13CYARows(waypoints, thisPage)), continueUrl))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event13) andThen requireData).async { implicit request =>
      connector.compileEvent(request.pstr, request.userAnswers.eventDataIdentifier(Event13)).map {
        _ =>
          Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
      }
    }

  private def buildEvent13CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    Seq(
      SchemeStructureSummary.row(request.userAnswers, waypoints, sourcePage),
      SchemeStructureDescriptionSummary.row(request.userAnswers, waypoints, sourcePage),
      ChangeDateSummary.row(request.userAnswers, waypoints, sourcePage)
    ).flatten
  }
}
