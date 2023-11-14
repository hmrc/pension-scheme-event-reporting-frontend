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

package controllers.event6

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.ReadOnlyCYA
import models.Index
import models.enumeration.EventType.Event6
import models.requests.DataRequest
import pages.event6.Event6CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CompileService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswersValidation
import viewmodels.checkAnswers.MembersDetailsSummary
import viewmodels.event6.checkAnswers.{AmountCrystallisedAndDateSummary, InputProtectionTypeSummary, TypeOfProtectionSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event6CheckYourAnswersController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  compileService: CompileService,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckYourAnswersView,
                                                  userAnswersValidation: UserAnswersValidation
                                                )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(Event6) andThen requireData) { implicit request =>
      val thisPage = Event6CheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event6.routes.Event6CheckYourAnswersController.onClick(index).url
      val version = request.userAnswers.get(VersionInfoPage).map(_.version)
      val readOnlyHeading = ReadOnlyCYA.readOnlyHeading(Event6, version, request.readOnly())
      Ok(view(SummaryListViewModel(rows = buildEvent6CYARows(waypoints, thisPage, index)), continueUrl, readOnlyHeading))
    }

  def onClick(index: Index): Action[AnyContent] =
    (identify andThen getData(Event6) andThen requireData).async { implicit request =>
      userAnswersValidation.validate(Event6, index)
    }

  private def buildEvent6CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage, request.readOnly(), Event6).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage, request.readOnly(), Event6).toSeq ++
      TypeOfProtectionSummary.row(request.userAnswers, waypoints, index, sourcePage, request.readOnly()).toSeq ++
      InputProtectionTypeSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly(), index).toSeq ++
      AmountCrystallisedAndDateSummary.rowCrystallisedValue(request.userAnswers, waypoints, sourcePage, request.readOnly(), index).toSeq ++
      AmountCrystallisedAndDateSummary.rowCrystallisedDate(request.userAnswers, waypoints, sourcePage, request.readOnly(), index).toSeq
  }
}
