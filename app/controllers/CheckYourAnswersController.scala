/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event18, WindUp}
import models.requests.DataRequest
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{Event18ConfirmationSummary, SchemeWindUpDateSummary}
import viewmodels.event1.checkAnswers.{DoYouHoldSignedMandateSummary, MemberPaymentNatureDescriptionSummary, MembersDetailsSummary, PaymentNatureSummary, ValueOfUnauthorisedPaymentSummary}
import viewmodels.event1.employer.checkAnswers.CompanyDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>

    val thisPage = CheckYourAnswersPage(eventType)
    val waypoints = EmptyWaypoints

    val rows = eventType match {
      case WindUp => buildEventWindUpCYARows(waypoints, thisPage)
      case Event1 => buildEvent1CYARows(waypoints, thisPage)
      case Event18 => buildEvent18CYARows(waypoints, thisPage)
      case _ => Nil
    }

    Ok(view(SummaryListViewModel(rows = rows)))
  }

  private def buildEvent1CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, sourcePage).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, sourcePage).toSeq ++
      DoYouHoldSignedMandateSummary.row(request.userAnswers, waypoints, sourcePage).toSeq ++
      ValueOfUnauthorisedPaymentSummary.row(request.userAnswers, waypoints, sourcePage).toSeq ++
      PaymentNatureSummary.row(request.userAnswers, waypoints, sourcePage).toSeq ++
      MemberPaymentNatureDescriptionSummary.row(request.userAnswers, waypoints, sourcePage).toSeq ++
      CompanyDetailsSummary.row(request.userAnswers, waypoints, sourcePage).toSeq

  private def buildEventWindUpCYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    SchemeWindUpDateSummary.row(request.userAnswers, waypoints, sourcePage).toSeq

  private def buildEvent18CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    Event18ConfirmationSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
}
