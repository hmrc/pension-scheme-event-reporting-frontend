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

package controllers.common

import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.common.AnnualAllowanceSummaryFormProvider
import models.UserAnswers
import models.common.MembersSummary
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
import pages.Waypoints
import pages.common.{AnnualAllowanceSummaryPage, MembersPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{Message, SummaryListRowWithTwoValues}
import views.html.common.AnnualAllowanceSummaryView

import javax.inject.Inject

class AnnualAllowanceSummaryController @Inject()(
                                                  val controllerComponents: MessagesControllerComponents,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  userAnswersCacheConnector: UserAnswersCacheConnector,
                                                  formProvider: AnnualAllowanceSummaryFormProvider,
                                                  view: AnnualAllowanceSummaryView
                                                ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val form = formProvider(eventType)
      val mappedMember = request.userAnswers
        .getAll(MembersPage(eventType))(MembersSummary.readsMember).zipWithIndex.map {
        case (memberSummary, index) =>
          SummaryListRowWithTwoValues(
            key = memberSummary.name,
            firstValue = memberSummary.nINumber,
            secondValue = memberSummary.PaymentValue.toString,
            actions = Some(Actions(
              items = Seq(
                ActionItem(
                  content = Text(Message("site.view")),
                  href = eventType match {
                    case Event22 => controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(index).url
                    case Event23 => controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(index).url
                  }
                ),
                ActionItem(
                  content = Text(Message("site.remove")),
                  href = "#"
                )
              )
            ))
          )
      }
      Ok(view(form, waypoints, eventType, mappedMember, sumValue(request.userAnswers, eventType)))
    }

  private def sumValue(userAnswers: UserAnswers, eventType: EventType) =  userAnswers.sumAll(MembersPage(eventType), MembersSummary.readsMemberValue)

  def onSubmit(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) {
    implicit request =>
      val form = formProvider(eventType)
      form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(view(formWithErrors, waypoints, eventType, Nil, sumValue(request.userAnswers, eventType)))
        },
        value => {
          val userAnswerUpdated = request.userAnswers.setOrException(AnnualAllowanceSummaryPage(eventType), value)
          Redirect(AnnualAllowanceSummaryPage(eventType).navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)}
      )
  }
}
