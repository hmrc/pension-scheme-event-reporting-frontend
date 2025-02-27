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

package controllers.event1

import controllers.actions._
import forms.event1.UnauthPaymentSummaryFormProvider
import forms.mappings.Formatters
import models.enumeration.EventType
import models.enumeration.EventType.Event1
import models.event1.MembersOrEmployersSummary
import models.{Index, TaxYear, UserAnswers}
import pages.common.MembersOrEmployersPage
import pages.event1.UnauthPaymentSummaryPage
import pages.{EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import services.EventPaginationService
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.Message
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import views.html.event1.UnauthPaymentSummaryView

import javax.inject.Inject

class UnauthPaymentSummaryController @Inject()(
                                                val controllerComponents: MessagesControllerComponents,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: UnauthPaymentSummaryFormProvider,
                                                view: UnauthPaymentSummaryView,
                                                eventPaginationService: EventPaginationService
                                              ) extends FrontendBaseController with I18nSupport with Formatters {

  private val form = formProvider()


  def onPageLoad(waypoints: Waypoints, search: Option[String]): Action[AnyContent] = onPageLoadPaginated(waypoints, search, Index(0))
  def onSubmit(waypoints: Waypoints): Action[AnyContent] = onSubmitPaginated(waypoints, Index(0))

  def onPageLoadPaginated(waypoints: Waypoints, search: Option[String] = None, page: Index): Action[AnyContent] =
    (identify andThen getData(EventType.Event1) andThen requireData) { implicit request =>
      val taxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers)
      val mappedMemberOrEmployer = getMappedMemberOrEmployer(request.userAnswers, request.readOnly(), search.map(_.toLowerCase))
      val pageTitle = if(search.isDefined && search != Some("")) {
        Messages("unauthPaymentSummary.title.search", search.getOrElse(""), taxYear)
      } else {
        Messages("unauthPaymentSummary.title", taxYear)
      }

      Ok(view(form,
        pageTitle,
        waypoints,
        mappedMemberOrEmployer,
        eventPaginationService.paginateMappedMembers(mappedMemberOrEmployer, page),
        page,
        sumValue(request.userAnswers),
        taxYear,
        search,
        routes.UnauthPaymentSummaryController.onPageLoadPaginated(waypoints, None, page).url))
  }

  private def sumValue(userAnswers: UserAnswers): String =
    currencyFormatter.format(userAnswers.sumAll(MembersOrEmployersPage(EventType.Event1), MembersOrEmployersSummary.readsMemberOrEmployerValue))

  def onSubmitPaginated(waypoints: Waypoints, page: Index): Action[AnyContent] = (identify andThen getData(EventType.Event1) andThen requireData) {
    implicit request =>
      val taxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers)
      form.bindFromRequest().fold(
        formWithErrors => {
          val mappedMemberOrEmployer = getMappedMemberOrEmployer(request.userAnswers, request.readOnly(), None)
          BadRequest(view(formWithErrors, Messages("unauthPaymentSummary.title", taxYear), waypoints, mappedMemberOrEmployer, eventPaginationService.paginateMappedMembers(mappedMemberOrEmployer,page), page, sumValue(request.userAnswers), taxYear, None,
            routes.UnauthPaymentSummaryController.onPageLoadPaginated(waypoints, None, page).url))
        },
        value => {
          val userAnswerUpdated = request.userAnswers.setOrException(UnauthPaymentSummaryPage, value)
          Redirect(UnauthPaymentSummaryPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)
        }
      )
  }

  private def getMappedMemberOrEmployer(userAnswers: UserAnswers,
                                        isReadOnly: Boolean,
                                        searchTerm: Option[String])
                                       (implicit messages: Messages): Seq[SummaryListRow] = {
    def searchTermFilter(membersSummary: MembersOrEmployersSummary) = searchTerm.forall { searchTerm =>
      val ninoMatches = membersSummary.nino.exists { nino => nino.toLowerCase.contains(searchTerm) }
      val companyNumberMatches = membersSummary.companyNumber.exists { companyNumber => companyNumber.toLowerCase.contains(searchTerm) }
      ninoMatches || membersSummary.name.toLowerCase.contains(searchTerm) || companyNumberMatches
    }

    userAnswers.getAll(MembersOrEmployersPage(EventType.Event1))(MembersOrEmployersSummary.readsMemberOrEmployer)
      .zipWithIndex.collect {
      case (memberOrEmployerSummary, index) if !(memberOrEmployerSummary.memberStatus.contains("Deleted") && searchTermFilter(memberOrEmployerSummary)) =>
        //TODO PODS-8617: Remove front-end filter. Values should be filtered via MongoDB with an index or by refactor
        val value = ValueViewModel(HtmlFormat.escape(currencyFormatter.format(memberOrEmployerSummary.unauthorisedPaymentValue)).toString)
        SummaryListRow(
          key = Key(
            content = Text(memberOrEmployerSummary.name)
          ),
          value = value,
          actions = Some(Actions(
            items = if (isReadOnly) {
              Seq(
                ActionItem(
                  content = Text(Message("site.view")),
                  href = controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(index).url
                )
              )
            } else {
              Seq(
                ActionItem(
                  content = Text(Message("site.view")),
                  href = controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(index).url
                ),
                ActionItem(
                  content = Text(Message("site.remove")),
                  href = controllers.common.routes.RemoveMemberController.onPageLoad(EmptyWaypoints, Event1, index).url
                )
              )
            }
          ))
        )
    }
  }
}
