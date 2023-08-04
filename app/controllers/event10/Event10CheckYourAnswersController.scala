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

package controllers.event10

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType.Event10
import models.requests.DataRequest
import pages.event10.Event10CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CompileService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.event10.checkAnswers.{BecomeOrCeaseSchemeSummary, ContractsOrPoliciesSummary, SchemeChangeDateSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event10CheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   compileService: CompileService,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData(Event10) andThen requireData) { implicit request =>
      val thisPage = Event10CheckYourAnswersPage()
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event10.routes.Event10CheckYourAnswersController.onClick.url
      Ok(view(SummaryListViewModel(rows = buildEvent10CYARows(waypoints, thisPage)), continueUrl))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event10) andThen requireData).async { implicit request =>
      compileService.compileEvent(Event10, request.pstr, request.userAnswers).map { _ =>
          Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
      }
    }

  private def buildEvent10CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    BecomeOrCeaseSchemeSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly()).toSeq ++
      SchemeChangeDateSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly()).toSeq ++
      ContractsOrPoliciesSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly())
  }
}
