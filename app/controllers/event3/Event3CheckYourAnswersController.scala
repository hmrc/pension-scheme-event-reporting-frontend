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

package controllers.event3

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.ReadOnlyCYA
import models.Index
import models.enumeration.EventType.Event3
import models.event3.ReasonForBenefits.Other
import models.requests.DataRequest
import pages.event3.{Event3CheckYourAnswersPage, ReasonForBenefitsPage}
import pages.{CheckAnswersPage, EmptyWaypoints, VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswersValidation
import viewmodels.checkAnswers.MembersDetailsSummary
import viewmodels.common.checkAnswers.PaymentDetailsSummary
import viewmodels.event3.checkAnswers.{EarlyBenefitsBriefDescriptionSummary, ReasonForBenefitsSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event3CheckYourAnswersController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckYourAnswersView,
                                                  userAnswersValidation: UserAnswersValidation
                                                )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(Event3) andThen requireData) { implicit request =>
      val thisPage = Event3CheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event3.routes.Event3CheckYourAnswersController.onClick(index).url
      val version = request.userAnswers.get(VersionInfoPage).map(_.version)
      val readOnlyHeading = ReadOnlyCYA.readOnlyHeading(Event3, version, request.readOnly())
      Ok(view(SummaryListViewModel(rows = buildEvent3CYARows(waypoints, thisPage, index)), continueUrl, readOnlyHeading))
    }

  def onClick(index: Index): Action[AnyContent] =
    (identify andThen getData(Event3) andThen requireData).async { implicit request =>
      userAnswersValidation.validate(Event3, index)
    }

  private def event3ReasonForBenefitsRows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Int)
                                         (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    request.userAnswers.get(ReasonForBenefitsPage(index)) match {
      case Some(Other) => EarlyBenefitsBriefDescriptionSummary.row(request.userAnswers, waypoints, index, sourcePage, request.readOnly()).toSeq
      case _ => Nil
    }
  }

  private def buildEvent3CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowMembersDetails(request.userAnswers, waypoints, index, sourcePage, request.readOnly(), Event3).toSeq ++
      ReasonForBenefitsSummary.row(request.userAnswers, waypoints, index, sourcePage, request.readOnly()).toSeq ++
      event3ReasonForBenefitsRows(waypoints, sourcePage, index) ++
      PaymentDetailsSummary.rowPaymentDetails(request.userAnswers, waypoints, sourcePage, request.readOnly(), Event3, index).toSeq
  }
}
