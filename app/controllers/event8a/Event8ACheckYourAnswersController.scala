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

package controllers.event8a

import com.google.inject.Inject
import connectors.EventReportingConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.Index
import models.enumeration.EventType.Event8A
import models.requests.DataRequest
import pages.event8a.Event8ACheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.MembersDetailsSummary
import viewmodels.event8a.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event8ACheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   connector: EventReportingConnector,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(Event8A) andThen requireData) { implicit request =>
      val thisPage = Event8ACheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event8a.routes.Event8ACheckYourAnswersController.onClick.url
      Ok(view(SummaryListViewModel(rows = buildEvent8aCYARows(waypoints, thisPage, index)), continueUrl))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event8A) andThen requireData).async { implicit request =>
      connector.compileEvent(request.pstr, Event8A).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, Event8A).url)
      }
    }

  private def buildEvent8aCYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage, Event8A).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage, Event8A).toSeq ++
      TypeOfProtectionSummary.row(request.userAnswers, waypoints, index, sourcePage, Event8A).toSeq ++
      TypeOfProtectionReferenceSummary.row(request.userAnswers, waypoints, sourcePage, Event8A, index).toSeq ++
      LumpSumAmountAndDateSummary.rowLumpSumValue(request.userAnswers, waypoints, sourcePage, Event8A, index).toSeq ++
      LumpSumAmountAndDateSummary.rowLumpSumDate(request.userAnswers, waypoints, sourcePage, Event8A, index).toSeq
  }
}
