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

import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.common.MembersSummaryFormProvider
import forms.mappings.Formatters
import models.{Index, UserAnswers}
import models.common.MembersSummary
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
import pages.Waypoints
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
                                                  userAnswersCacheConnector: UserAnswersCacheConnector,
                                                  formProvider: MembersSummaryFormProvider,
                                                  view: MembersSummaryView,
                                                  newView: MembersSummaryViewWithPagination,
                                                  eventPaginationService: EventPaginationService
                                                ) extends FrontendBaseController with I18nSupport with Formatters {

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers, eventType)
      if (mappedMembers.length > 25) {
        Redirect(routes.MembersSummaryController.onPageLoadWithPageNumber(waypoints, 0))
      } else {
        Ok(view(form, waypoints, eventType, mappedMembers, sumValue(request.userAnswers, eventType)))
      }
    }

  def onPageLoadWithPageNumber(waypoints: Waypoints, eventType: EventType, pageNumber: Index): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers, eventType)
      val paginationStats = eventPaginationService.paginateMappedMembers(mappedMembers, pageNumber)
      if (mappedMembers.length < 25) {
        Redirect(routes.MembersSummaryController.onPageLoad(waypoints, eventType))
      } else {
        Ok(newView(form, waypoints, eventType, mappedMembers, sumValue(request.userAnswers, eventType), paginationStats, pageNumber))
      }
    }

  private def sumValue(userAnswers: UserAnswers, eventType: EventType) =
    currencyFormatter.format(userAnswers.sumAll(MembersPage(eventType), MembersSummary.readsMemberValue))

  def onSubmit(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) {
    implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers, eventType)
      form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(view(formWithErrors, waypoints, eventType, mappedMembers, sumValue(request.userAnswers, eventType)))
        },
        value => {
          val userAnswerUpdated = request.userAnswers.setOrException(MembersSummaryPage(eventType, 0), value)
          Redirect(MembersSummaryPage(eventType, 0).navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)}
      )
  }

  def onSubmitWithPageNumber(waypoints: Waypoints, eventType: EventType, pageNumber: Index): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) {
    implicit request =>
      val form = formProvider(eventType)
      val mappedMembers = getMappedMembers(request.userAnswers, eventType)
      val paginationStats = eventPaginationService.paginateMappedMembers(mappedMembers, pageNumber)
      form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(newView(formWithErrors, waypoints, eventType, mappedMembers, sumValue(request.userAnswers, eventType), paginationStats, pageNumber))
        },
        value => {
          val userAnswerUpdated = request.userAnswers.setOrException(MembersSummaryPage(eventType, pageNumber), value)
          Redirect(MembersSummaryPage(eventType, pageNumber).navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)}
      )
  }

   private def getMappedMembers(userAnswers : UserAnswers, eventType: EventType) (implicit messages: Messages) : Seq[SummaryListRowWithTwoValues] = {
    userAnswers.getAll(MembersPage(eventType))(MembersSummary.readsMember).zipWithIndex.map {
      case (memberSummary, index) =>
        SummaryListRowWithTwoValues(
          key = memberSummary.name,
          firstValue = memberSummary.nINumber,
          secondValue = currencyFormatter.format(memberSummary.PaymentValue),
          actions = Some(Actions(
            items = Seq(
              ActionItem(
                content = Text(Message("site.view")),
                href = eventType match {
                  case Event22 => controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(index).url
                  case Event23 => controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(index).url
                  case _ => "#"
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
  }
}
