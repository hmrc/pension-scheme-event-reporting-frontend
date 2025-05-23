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

package controllers.event24

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.ReadOnlyCYA
import models.Index
import models.enumeration.EventType
import models.enumeration.EventType.Event24
import models.event24.TypeOfProtectionGroup1.SchemeSpecific
import models.requests.DataRequest
import pages.event24._
import pages.{CheckAnswersPage, EmptyWaypoints, VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswersValidation
import viewmodels.checkAnswers.MembersDetailsSummary
import viewmodels.event24.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event24CheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView,
                                                   userAnswersValidation: UserAnswersValidation
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  private val eventType = EventType.Event24

  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val thisPage = Event24CheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event24.routes.Event24CheckYourAnswersController.onClick(index).url
      val version = request.userAnswers.get(VersionInfoPage).map(_.version)
      val readOnlyHeading = ReadOnlyCYA.readOnlyHeading(eventType, version, request.readOnly())
      Ok(view(SummaryListViewModel(rows = buildEvent24CYARows(waypoints, thisPage, index)), continueUrl, readOnlyHeading))
    }

  def onClick(index: Index): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData).async { implicit request =>
      userAnswersValidation.validate(Event24, index)
    }

  private def buildEvent24CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowMembersDetails(request.userAnswers, waypoints, index, sourcePage, request.readOnly(), eventType).toSeq ++
      CrystallisedDateSummary.rowCrystallisedDate(request.userAnswers, waypoints, sourcePage, request.readOnly(), index) ++
      BCETypeSelectionSummary.rowBCETypeSelection(request.userAnswers, waypoints, index, sourcePage, request.readOnly()) ++
      TotalAmountBenefitCrystallisationSummary.row(request.userAnswers, waypoints, sourcePage, index) ++
      ValidProtectionSummary.row(request.userAnswers, waypoints, sourcePage, index) ++
      referencesRow(waypoints, index, sourcePage) ++
      hasSchemeSpecificRow(waypoints, index, sourcePage) ++
      TypeOfProtectionGroup2Summary.row(request.userAnswers, waypoints, index, sourcePage, request.readOnly()) ++
      OverAllowanceAndDeathBenefitSummary.row(request.userAnswers, waypoints, sourcePage, index) ++
      hasOverAllowanceRow(waypoints, index, sourcePage)
  }

  private def hasOverAllowanceRow(waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    request.userAnswers.get(OverAllowanceAndDeathBenefitPage(index)) match {
      case Some(true) =>
        (MarginalRateSummary.row(request.userAnswers, waypoints, sourcePage, index) ++
          hasMarginalRateRow(waypoints, index, sourcePage)).toSeq
      case _ =>
        request.userAnswers.get(OverAllowancePage(index)) match {
          case Some(true) =>
            (OverAllowanceSummary.row(request.userAnswers, waypoints, sourcePage, index) ++
              MarginalRateSummary.row(request.userAnswers, waypoints, sourcePage, index) ++
              hasMarginalRateRow(waypoints, index, sourcePage)).toSeq
          case _ =>
            OverAllowanceSummary.row(request.userAnswers, waypoints, sourcePage, index).toSeq
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

  private def referencesRow(waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                                (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    request.userAnswers.get(TypeOfProtectionGroup1Page(index)) match {
      case Some(protectionTypes) => EnhancementsSummary.row(protectionTypes, request.userAnswers, waypoints, index, sourcePage, request.readOnly()).toSeq
      case _ => Nil
    }
  }

  private def hasSchemeSpecificRow(waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                                      (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    request.userAnswers.get(TypeOfProtectionGroup1Page(index)) match {
      case Some(protectionTypes) => {
        if (protectionTypes.contains(SchemeSpecific)) {
          val summaryRow = SchemeSpecificSummary.row(waypoints, index, sourcePage, request.readOnly())
          Seq(summaryRow)
        } else {
          Nil
        }
      }
      case _ => Nil
    }
  }
}
