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

package controllers.event2

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.ReadOnlyCYA
import models.Index
import models.enumeration.EventType.Event2
import models.requests.DataRequest
import pages.event2.Event2CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Event2MemberPageNumbers, UserAnswersValidation}
import viewmodels.checkAnswers.MembersDetailsSummary
import viewmodels.event2.checkAnswers.{AmountPaidSummary, DatePaidSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event2CheckYourAnswersController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckYourAnswersView,
                                                  userAnswersValidation: UserAnswersValidation
                                                )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(Event2) andThen requireData) { implicit request =>
      val thisPage = Event2CheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event2.routes.Event2CheckYourAnswersController.onClick(index).url
      val version = request.userAnswers.get(VersionInfoPage).map(_.version)
      val readOnlyHeading = ReadOnlyCYA.readOnlyHeading(Event2, version, request.readOnly())
      Ok(view(SummaryListViewModel(rows = buildEvent2CYARows(waypoints, thisPage, index)), continueUrl, readOnlyHeading))
    }

  def onClick(index: Index): Action[AnyContent] =
    (identify andThen getData(Event2) andThen requireData).async { implicit request =>
      userAnswersValidation.validate(Event2, index)
    }

  private def buildEvent2CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowMembersDetails(request.userAnswers, waypoints, index, sourcePage, request.readOnly(),
      Event2, Event2MemberPageNumbers.FIRST_PAGE_DECEASED).toSeq ++
      MembersDetailsSummary.rowMembersDetails(request.userAnswers, waypoints, index, sourcePage, request.readOnly(),
        Event2, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY).toSeq ++
      AmountPaidSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly(), index).toSeq ++
      DatePaidSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly(), index).toSeq
  }
}
