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

package controllers.event1

import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.event1.UnauthPaymentSummaryFormProvider
import forms.mappings.Formatters
import models.UserAnswers
import models.enumeration.EventType
import models.event1.MembersOrEmployersSummary
import pages.Waypoints
import pages.common.MembersOrEmployersPage
import pages.event1.UnauthPaymentSummaryPage
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
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
                                                userAnswersCacheConnector: UserAnswersCacheConnector,
                                                formProvider: UnauthPaymentSummaryFormProvider,
                                                view: UnauthPaymentSummaryView
                                              ) extends FrontendBaseController with I18nSupport with Formatters {

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(EventType.Event1) andThen requireData) { implicit request =>
    val mappedMemberOrEmployer = getMappedMemberOrEmployer(request.userAnswers)
    Ok(view(form, waypoints, mappedMemberOrEmployer, sumValue(request.userAnswers)))
  }

  private def sumValue(userAnswers: UserAnswers) =
    currencyFormatter.format(userAnswers.sumAll(MembersOrEmployersPage(EventType.Event1), MembersOrEmployersSummary.readsMemberOrEmployerValue))

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(EventType.Event1) andThen requireData) {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val mappedMemberOrEmployer = getMappedMemberOrEmployer(request.userAnswers)
          BadRequest(view(formWithErrors, waypoints, mappedMemberOrEmployer, sumValue(request.userAnswers)))
        },
        value => {
          val userAnswerUpdated = request.userAnswers.setOrException(UnauthPaymentSummaryPage, value)
          Redirect(UnauthPaymentSummaryPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)}
      )
  }

  private def getMappedMemberOrEmployer(userAnswers: UserAnswers)  (implicit messages: Messages)  : Seq[SummaryListRow] = {
    userAnswers.getAll(MembersOrEmployersPage(EventType.Event1))(MembersOrEmployersSummary.readsMemberOrEmployer).zipWithIndex.map {
      case (memberOrEmployerSummary, index) =>
        val value = ValueViewModel(HtmlFormat.escape(currencyFormatter.format(memberOrEmployerSummary.unauthorisedPaymentValue)).toString)
        SummaryListRow(
          key = Key(
            content = Text(memberOrEmployerSummary.name)
          ),
          value = value,
          actions = Some(Actions(
            items = Seq(
              ActionItem(
                content = Text(Message("site.view")),
                href = controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(index).url
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
