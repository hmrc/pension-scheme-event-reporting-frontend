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

package controllers.event19

import com.google.inject.Inject
import connectors.EventReportingConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType.Event19
import models.requests.DataRequest
import pages.event19.Event19CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.event19.checkAnswers.{CountryOrTerritorySummary, DateChangeMadeSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event19CheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   connector: EventReportingConnector,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView,
                                                   countryOrTerritorySummary: CountryOrTerritorySummary
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData(Event19) andThen requireData) { implicit request =>
      val thisPage = Event19CheckYourAnswersPage
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event19.routes.Event19CheckYourAnswersController.onClick.url
      Ok(view(SummaryListViewModel(rows = buildEvent19CYARows(waypoints, thisPage)), continueUrl))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event19) andThen requireData).async { implicit request =>
      connector.compileEvent(request.pstr, Event19).map {
        _ =>
          Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
      }
    }

  private def buildEvent19CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    Seq(
      countryOrTerritorySummary.row(request.userAnswers, waypoints, sourcePage),
      DateChangeMadeSummary.row(request.userAnswers, waypoints, sourcePage)
    ).flatten
  }
}
