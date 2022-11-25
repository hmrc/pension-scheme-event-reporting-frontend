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

package controllers.event23

import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.event23.AnnualAllowanceSummaryFormProvider
import models.UserAnswers
import models.common.MembersSummary
import models.enumeration.EventType
import pages.Waypoints
import pages.common.MembersPage
import pages.event23.AnnualAllowanceSummaryPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{Message, SummaryListRowWithTwoValues}
import views.html.event23.AnnualAllowanceSummaryView

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

  private val form = formProvider()
  private val eventType = EventType.Event23

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
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
              href = controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(index).url
            ),
            ActionItem(
              content = Text(Message("site.remove")),
              href = "#"
            )
          )
        ))
      )
      }
    Ok(view(form, waypoints, mappedMember, sumValue(request.userAnswers)))
  }

  private def sumValue(userAnswers: UserAnswers) =  userAnswers.sumAll(MembersPage(eventType), MembersSummary.readsMemberValue)

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(view(formWithErrors, waypoints, Nil, sumValue(request.userAnswers)))
        },
        value => {
          val userAnswerUpdated = request.userAnswers.setOrException(AnnualAllowanceSummaryPage, value)
          Redirect(AnnualAllowanceSummaryPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)}
    )
  }
}
