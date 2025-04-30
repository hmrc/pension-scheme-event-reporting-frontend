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

package controllers.event11

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.ReadOnlyCYA
import models.UserAnswers
import models.enumeration.EventType.Event11
import models.requests.DataRequest
import pages.event11.{Event11CheckYourAnswersPage, HasSchemeChangedRulesInvestmentsInAssetsPage, HasSchemeChangedRulesPage}
import pages.{CheckAnswersPage, EmptyWaypoints, VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswersValidation
import viewmodels.event11.checkAnswers.{
  HasSchemeChangedRulesInvestmentsInAssetsSummary,
  HasSchemeChangedRulesSummary,
  InvestmentsInAssetsRuleChangeDateSummary,
  UnAuthPaymentsRuleChangeDateSummary
}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event11CheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView,
                                                   userAnswersValidation: UserAnswersValidation
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData(Event11) andThen requireData) { implicit request =>
      val thisPage = Event11CheckYourAnswersPage()
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event11.routes.Event11CheckYourAnswersController.onClick.url
      val version = request.userAnswers.get(VersionInfoPage).map(_.version)
      val readOnlyHeading = ReadOnlyCYA.readOnlyHeading(Event11, version, request.readOnly())

      Ok(view(SummaryListViewModel(rows = buildEvent11CYARows(waypoints, thisPage, request.userAnswers)), continueUrl, readOnlyHeading))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event11) andThen requireData).async { implicit request =>
      userAnswersValidation.validate(Event11)
    }

  private def buildEvent11CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, answers: UserAnswers)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    
    val optRowUnauthPayments = if (answers.get(HasSchemeChangedRulesPage).getOrElse(false)) {
      UnAuthPaymentsRuleChangeDateSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly())
    } else Nil
    val optRowInvestmentsInAssets = if (answers.get(HasSchemeChangedRulesInvestmentsInAssetsPage).getOrElse(false)) {
      InvestmentsInAssetsRuleChangeDateSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly())
    } else Nil

    Seq(
      HasSchemeChangedRulesSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly()) ++
        optRowUnauthPayments ++
        HasSchemeChangedRulesInvestmentsInAssetsSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly()) ++
        optRowInvestmentsInAssets
    ).flatten
  }
}
