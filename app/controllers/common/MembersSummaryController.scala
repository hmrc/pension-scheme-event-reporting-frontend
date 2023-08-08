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

package controllers.common

import controllers.actions._
import forms.common.MembersSummaryFormProvider
import forms.mappings.Formatters
import models.TaxYear.getSelectedTaxYearAsString
import models.common.MembersSummary
import models.enumeration.EventType
import models.enumeration.EventType.{Event2, Event22, Event23, Event3, Event4, Event5, Event6, Event8, Event8A}
import models.{Index, UserAnswers}
import pages.{EmptyWaypoints, Waypoints}
import pages.common.{MembersPage, MembersSummaryPage}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.EventPaginationService
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{Message, SummaryListRowWithTwoValues}
import views.html.common.{MembersSummaryView, MembersSummaryViewWithPagination}

import javax.inject.Inject

//scalastyle:off
class MembersSummaryController @Inject()(
                                          val controllerComponents: MessagesControllerComponents,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: MembersSummaryFormProvider,
                                          view: MembersSummaryView,
                                          newView: MembersSummaryViewWithPagination,
                                          eventPaginationService: EventPaginationService
                                        ) extends FrontendBaseController with I18nSupport with Formatters {

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers, eventType)
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      if (mappedMembers.length > 25) {
        Redirect(routes.MembersSummaryController.onPageLoadWithPageNumber(waypoints, eventType, 0))
      } else {
        Ok(view(form, waypoints, eventType, mappedMembers, sumValue(request.userAnswers, eventType), selectedTaxYear))
      }
    }

  def onPageLoadWithPageNumber(waypoints: Waypoints, eventType: EventType, pageNumber: Index): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers, eventType)
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      val paginationStats = eventPaginationService.paginateMappedMembers(mappedMembers, pageNumber)
      if (mappedMembers.length <= 25) {
        Redirect(routes.MembersSummaryController.onPageLoad(waypoints, eventType))
      } else {
        Ok(newView(form, waypoints, eventType, mappedMembers, sumValue(request.userAnswers, eventType), selectedTaxYear, paginationStats, pageNumber))
      }
    }

  private def sumValue(userAnswers: UserAnswers, eventType: EventType) =
    currencyFormatter.format(userAnswers.sumAll(MembersPage(eventType), MembersSummary.readsMemberValue(eventType)))

  def onSubmit(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) {
    implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers, eventType)
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(view(formWithErrors, waypoints, eventType, mappedMembers, sumValue(request.userAnswers, eventType), selectedTaxYear))
        },
        value => {
          val userAnswerUpdated = request.userAnswers.setOrException(MembersSummaryPage(eventType, 0), value)
          Redirect(MembersSummaryPage(eventType, 0).navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)
        }
      )
  }

  private def getMappedMembers(userAnswers: UserAnswers, eventType: EventType)(implicit messages: Messages): Seq[SummaryListRowWithTwoValues] = {
    userAnswers.getAll(MembersPage(eventType))(MembersSummary.readsMember(eventType)).zipWithIndex.collect {
      case (memberSummary, index) if !memberSummary.memberStatus.contains("Deleted") =>
        //TODO PODS-8617: Remove front-end filter. Values should be filtered via MongoDB with an index or by refactor
        SummaryListRowWithTwoValues(
          key = memberSummary.name,
          firstValue = memberSummary.nINumber,
          secondValue = currencyFormatter.format(memberSummary.PaymentValue),
          actions = Some(Actions(
            items = Seq(
              ActionItem(
                content = Text(Message("site.view")),
                href = eventType match {
                  case Event2 => controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(index).url
                  case Event3 => controllers.event3.routes.Event3CheckYourAnswersController.onPageLoad(index).url
                  case Event4 => controllers.event4.routes.Event4CheckYourAnswersController.onPageLoad(index).url
                  case Event5 => controllers.event5.routes.Event5CheckYourAnswersController.onPageLoad(index).url
                  case Event6 => controllers.event6.routes.Event6CheckYourAnswersController.onPageLoad(index).url
                  case Event8 => controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(index).url
                  case Event8A => controllers.event8a.routes.Event8ACheckYourAnswersController.onPageLoad(index).url
                  case Event22 => controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(index).url
                  case Event23 => controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(index).url
                  case _ => "#"
                }
              ),
              ActionItem(
                content = Text(Message("site.remove")),
                href = controllers.common.routes.RemoveMemberController.onPageLoad(EmptyWaypoints, eventType, index).url
              )
            )
          ))
        )
    }
  }
}
