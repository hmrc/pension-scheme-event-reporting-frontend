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

package controllers.event4

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.ReadOnlyCYA
import models.{Index, MemberSummaryPath}
import models.enumeration.EventType.Event4
import models.requests.DataRequest
import pages.event4.Event4CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CompileService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.MembersDetailsSummary
import viewmodels.common.checkAnswers.PaymentDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event4CheckYourAnswersController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  compileService: CompileService,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckYourAnswersView
                                                )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(Event4) andThen requireData) { implicit request =>
      val thisPage = Event4CheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event4.routes.Event4CheckYourAnswersController.onClick.url
      val version = request.userAnswers.get(VersionInfoPage).map(_.version)
      val readOnlyHeading = ReadOnlyCYA.readOnlyHeading(Event4, version, request.readOnly())
      Ok(view(SummaryListViewModel(rows = buildEvent4CYARows(waypoints, thisPage, index)), continueUrl, readOnlyHeading))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event4) andThen requireData).async { implicit request =>
      compileService.compileEvent(Event4, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event4)).url)
      }
    }

  private def buildEvent4CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage, request.readOnly(), Event4).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage, request.readOnly(), Event4).toSeq ++
      PaymentDetailsSummary.rowAmountPaid(request.userAnswers, waypoints, sourcePage, request.readOnly(), Event4, index).toSeq ++
      PaymentDetailsSummary.rowEventDate(request.userAnswers, waypoints, sourcePage, request.readOnly(), Event4, index).toSeq
  }
}
