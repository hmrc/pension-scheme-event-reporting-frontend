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

package controllers.event7

import controllers.actions._
import forms.common.MembersSummaryFormProvider
import forms.mappings.Formatters
import models.TaxYear.getSelectedTaxYearAsString
import models.enumeration.EventType.Event7
import models.event7.Event7MembersSummary
import models.{Index, UserAnswers}
import pages.common.MembersSummaryPage
import pages.event7.Event7MembersPage
import pages.{EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.EventPaginationService
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{Message, SummaryListRowWithThreeValues}
import views.html.event7.Event7MembersSummaryView

import javax.inject.Inject

//scalastyle:off
class Event7MembersSummaryController @Inject()(
                                                val controllerComponents: MessagesControllerComponents,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: MembersSummaryFormProvider,
                                                view: Event7MembersSummaryView,
                                                eventPaginationService: EventPaginationService
                                              ) extends FrontendBaseController with I18nSupport with Formatters {
  private val eventType = Event7

  def onPageLoad(waypoints: Waypoints, search: Option[String] = None): Action[AnyContent] = {
    onPageLoadWithPageNumber(waypoints, Index(0), search)
  }

  def onPageLoadWithPageNumber(waypoints: Waypoints, pageNumber: Index, search: Option[String] = None): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers, request.readOnly(), search.map(_.toLowerCase))
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      val paginationStats = eventPaginationService.paginateMappedMembers(mappedMembers, pageNumber)
      Ok(view(form, waypoints, eventType, mappedMembers, sumValue(request.userAnswers), selectedTaxYear, paginationStats, pageNumber, search, routes.Event7MembersSummaryController.onPageLoad(waypoints, None).url))
    }

  private def sumValue(userAnswers: UserAnswers) =
    currencyFormatter.format(userAnswers.sumAll(Event7MembersPage(eventType), Event7MembersSummary.readsCombinedPayments))

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) {
    implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers, request.readOnly(), None)
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      form.bindFromRequest().fold(
        formWithErrors => {
          val paginationStats = eventPaginationService.paginateMappedMembers(mappedMembers, 0)
          BadRequest(view(formWithErrors, waypoints, eventType, mappedMembers, sumValue(request.userAnswers), selectedTaxYear, paginationStats, Index(0), None, routes.Event7MembersSummaryController.onPageLoad(waypoints, None).url))
        },
        value => {
          val userAnswerUpdated = request.userAnswers.setOrException(MembersSummaryPage(eventType, 0), value)
          Redirect(MembersSummaryPage(eventType, 0).navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)
        }
      )
  }

  private def getMappedMembers(userAnswers: UserAnswers, isReadOnly: Boolean, searchTerm: Option[String])(implicit messages: Messages): Seq[SummaryListRowWithThreeValues] = {
    def searchTermFilter(membersSummary: Event7MembersSummary) = searchTerm.forall { searchTerm =>
      membersSummary.nINumber.toLowerCase.contains(searchTerm) || membersSummary.name.toLowerCase.contains(searchTerm)
    }

    userAnswers.getAll(Event7MembersPage(eventType))(Event7MembersSummary.readsMember).zipWithIndex.collect {

      case (memberSummary, index) if !memberSummary.memberStatus.contains("Deleted") && searchTermFilter(memberSummary) =>
        SummaryListRowWithThreeValues(
          key = memberSummary.name,
          firstValue = memberSummary.nINumber,
          secondValue = decimalFormat.format(memberSummary.PaymentValue),
          thirdValue = decimalFormat.format(memberSummary.PaymentValueTwo),
          actions = Some(Actions(
            items = if (isReadOnly) {
              Seq(
                ActionItem(
                  content = Text(Message("site.view")),
                  href = controllers.event7.routes.Event7CheckYourAnswersController.onPageLoad(index).url
                )
              )
            } else {
              Seq(
                ActionItem(
                  content = Text(Message("site.view")),
                  href = controllers.event7.routes.Event7CheckYourAnswersController.onPageLoad(index).url
                ),
                ActionItem(
                  content = Text(Message("site.remove")),
                  href = controllers.common.routes.RemoveMemberController.onPageLoad(EmptyWaypoints, eventType, index).url
                )
              )
            }
          ))
        )
    }
  }
}
