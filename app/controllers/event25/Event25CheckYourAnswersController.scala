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

package controllers.event25

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.ReadOnlyCYA
import models.enumeration.EventType
import models.requests.DataRequest
import models.{Index, MemberSummaryPath}
import pages.event25.{Event25CheckYourAnswersPage, MarginalRatePage, OverAllowanceAndDeathBenefitPage, OverAllowancePage, ValidProtectionPage}
import pages.{CheckAnswersPage, EmptyWaypoints, VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CompileService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{EmployerPayeReferenceSummary, MarginalRateSummary, MembersDetailsSummary, OverAllowanceAndDeathBenefitSummary, TotalAmountBenefitCrystallisationSummary, ValidProtectionSummary}
import viewmodels.event25.checkAnswers.{BCETypeSelectionSummary, CrystallisedDateSummary, OverAllowanceSummary, TypeOfProtectionReferenceSummary, TypeOfProtectionSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event25CheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   compileService: CompileService,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  private val eventType = EventType.Event25
  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val thisPage = Event25CheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event25.routes.Event25CheckYourAnswersController.onClick.url
      val version = request.userAnswers.get(VersionInfoPage).map(_.version)
      val readOnlyHeading = ReadOnlyCYA.readOnlyHeading(eventType, version, request.readOnly())
      Ok(view(SummaryListViewModel(rows = buildEvent25CYARows(waypoints, thisPage, index)), continueUrl, readOnlyHeading))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData).async { implicit request =>
      compileService.compileEvent(eventType, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(eventType)).url)
      }
    }

  private def buildEvent25CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage, request.readOnly(), eventType).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage, request.readOnly(), eventType).toSeq ++
      CrystallisedDateSummary.rowCrystallisedDate(request.userAnswers, waypoints, sourcePage, request.readOnly(), index) ++
      BCETypeSelectionSummary.rowBCETypeSelection(request.userAnswers, waypoints, index, sourcePage, request.readOnly()) ++
      TotalAmountBenefitCrystallisationSummary.row(request.userAnswers, waypoints, sourcePage, index) ++
      ValidProtectionSummary.row(request.userAnswers, waypoints, sourcePage, index) ++
      hasValidProtectionRow(waypoints, index, sourcePage) ++
      OverAllowanceSummary.row(request.userAnswers, waypoints, sourcePage, index) ++
      hasOverAllowanceRow(waypoints, index, sourcePage) ++
      hasMarginalRateRow(waypoints, index, sourcePage)
  }

  private def hasValidProtectionRow(waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                                         (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    request.userAnswers.get(ValidProtectionPage(index)) match {
      case Some(true) =>
        (TypeOfProtectionSummary.row(request.userAnswers, waypoints, index, sourcePage, request.readOnly()) ++
          TypeOfProtectionReferenceSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly(), index)).toSeq
      case _ =>
        Nil
    }
  }

  private def hasOverAllowanceRow(waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                                   (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    request.userAnswers.get(OverAllowancePage(index)) match {
      case Some(true) =>
        MarginalRateSummary.row(request.userAnswers, waypoints, sourcePage, index).toSeq
      case _ =>
        request.userAnswers.get(OverAllowanceAndDeathBenefitPage(index)) match {
          case Some(true) =>
            (OverAllowanceAndDeathBenefitSummary.row(request.userAnswers, waypoints, sourcePage, index) ++
              MarginalRateSummary.row(request.userAnswers, waypoints, sourcePage, index)).toSeq
          case _ =>
            OverAllowanceAndDeathBenefitSummary.row(request.userAnswers, waypoints, sourcePage, index).toSeq
        }
    }
  }

  private def hasMarginalRateRow(waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                                   (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    request.userAnswers.get(MarginalRatePage(index)) match {
      case Some(true) =>
        EmployerPayeReferenceSummary.row(request.userAnswers, waypoints, sourcePage, index).toSeq
      case _ =>
        Nil
    }
  }
}
