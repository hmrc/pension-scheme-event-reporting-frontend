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

package controllers.event7

import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.common.MembersSummaryFormProvider
import forms.mappings.Formatters
import models.TaxYear.getSelectedTaxYearAsString
import models.enumeration.EventType.Event7
import models.event7.Event7MembersSummary
import models.{Index, UserAnswers}
import pages.Waypoints
import pages.common.MembersSummaryPage
import pages.event7.Event7MembersPage
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.EventPaginationService
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{Message, SummaryListRowWithThreeValues}
import views.html.event7.{Event7MembersSummaryView, Event7MembersSummaryViewWithPagination}

import java.text.DecimalFormat
import javax.inject.Inject

//scalastyle:off
class Event7MembersSummaryController @Inject()(
                                                  val controllerComponents: MessagesControllerComponents,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  userAnswersCacheConnector: UserAnswersCacheConnector,
                                                  formProvider: MembersSummaryFormProvider,
                                                  view: Event7MembersSummaryView,
                                                  newView: Event7MembersSummaryViewWithPagination,
                                                  eventPaginationService: EventPaginationService
                                                ) extends FrontendBaseController with I18nSupport with Formatters {
  private val eventType = Event7

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers)
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      if (mappedMembers.length > 25) {
        Redirect(routes.Event7MembersSummaryController.onPageLoadWithPageNumber(waypoints, 0))
      } else {
        Ok(view(form, waypoints, eventType, mappedMembers, sumValue(request.userAnswers), selectedTaxYear))
      }
    }

  def onPageLoadWithPageNumber(waypoints: Waypoints, pageNumber: Index): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers)
      val paginationStats = eventPaginationService.paginateMappedMembersThreeValues(mappedMembers, pageNumber)
      if (mappedMembers.length <= 25) {
        Redirect(routes.Event7MembersSummaryController.onPageLoad(waypoints))
      } else {
        Ok(newView(form, waypoints, eventType, mappedMembers, sumValue(request.userAnswers), paginationStats, pageNumber))
      }
    }

  private def sumValue(userAnswers: UserAnswers) =
    currencyFormatter.format(userAnswers.sumAll(Event7MembersPage(eventType), Event7MembersSummary.readsCombinedPayments))

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) {
    implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers)
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(view(formWithErrors, waypoints, eventType, mappedMembers, sumValue(request.userAnswers), selectedTaxYear))
        },
        value => {
          val userAnswerUpdated = request.userAnswers.setOrException(MembersSummaryPage(eventType, 0), value)
          Redirect(MembersSummaryPage(eventType, 0).navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)}
      )
  }

   private def getMappedMembers(userAnswers : UserAnswers) (implicit messages: Messages) : Seq[SummaryListRowWithThreeValues] = {
    userAnswers.getAll(Event7MembersPage(eventType))(Event7MembersSummary.readsMember).zipWithIndex.map {
      case (memberSummary, index) =>
        SummaryListRowWithThreeValues(
          key = memberSummary.name,
          firstValue = memberSummary.nINumber,
          secondValue = decimalFormat.format(memberSummary.PaymentValue),
          thirdValue = decimalFormat.format(memberSummary.PaymentValueTwo),
          actions = Some(Actions(
            items = Seq(
              ActionItem(
                content = Text(Message("site.view")),
                href = controllers.event7.routes.Event7CheckYourAnswersController.onPageLoad(index).url
              ),
              ActionItem(
                content = Text(Message("site.remove")),
                href = "#"
              )
            )
          ))
        )
    }
  }
}
