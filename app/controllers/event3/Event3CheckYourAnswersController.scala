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

package controllers.event3

import com.google.inject.Inject
import connectors.EventReportingConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.Index
import models.enumeration.EventType.{Event3, Event4}
import models.event3.ReasonForBenefits.Other
import models.requests.DataRequest
import pages.event3.{Event3CheckYourAnswersPage, ReasonForBenefitsPage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{MembersDetailsSummary, ReasonForBenefitsSummary}
import viewmodels.common.checkAnswers.PaymentDetailsSummary
import viewmodels.event3.checkAnswers.EarlyBenefitsBriefDescriptionSummary
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event3CheckYourAnswersController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  connector: EventReportingConnector,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckYourAnswersView
                                                )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(Event3) andThen requireData) { implicit request =>
      val thisPage = Event3CheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event3.routes.Event3CheckYourAnswersController.onClick.url
      Ok(view(SummaryListViewModel(rows = buildEvent3CYARows(waypoints, thisPage, index)), continueUrl))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event4) andThen requireData).async { implicit request =>
      connector.compileEvent(request.pstr, Event4).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, Event4).url)
      }
    }

  private def event3ReasonForBenefitsRows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Int)
                                           (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    request.userAnswers.get(ReasonForBenefitsPage(index)) match {
      case Some(Other) => EarlyBenefitsBriefDescriptionSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case _ => Nil
    }
  }

  private def buildEvent3CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage, Event3).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage, Event3).toSeq ++
      ReasonForBenefitsSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq ++
      event3ReasonForBenefitsRows(waypoints, sourcePage, index) ++
      PaymentDetailsSummary.rowAmountPaid(request.userAnswers, waypoints, sourcePage, Event3, index).toSeq ++
      PaymentDetailsSummary.rowEventDate(request.userAnswers, waypoints, sourcePage, Event3, index).toSeq
  }
}
