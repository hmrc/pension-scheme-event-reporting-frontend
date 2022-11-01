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
import models.UserAnswers
import models.enumeration.EventType
import pages.Waypoints
import pages.event1.{MembersOrEmployersPage, UnauthPaymentSummaryPage}
import play.api.i18n.I18nSupport
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
import scala.concurrent.ExecutionContext
// TODO bring in correct information for table no tests have been added yet
class UnauthPaymentSummaryController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: UnauthPaymentSummaryFormProvider,
                                        view: UnauthPaymentSummaryView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event1

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    val mappedMemberOrEmployer = request.userAnswers
      .getAll(MembersOrEmployersPage)(MembersOrEmployersPage.readsMemberOrEmployer).map { memberOrEmployerSummary =>

      val value = ValueViewModel(HtmlFormat.escape(memberOrEmployerSummary.unauthorisedPaymentValue.toString()).toString)

        SummaryListRow(
          key = Key(
            content = Text(memberOrEmployerSummary.name)
          ),
          value = value,
          actions = Some(Actions(
            items = Seq(
              ActionItem(
                content = Text(Message("site.view")),
                href = "#"
              ),
              ActionItem(
                content = Text(Message("site.remove")),
                href = "#"
              )
            )
          ))
        )
      }
    val total = BigDecimal(0) // request.userAnswers.memberOrEmployerSummaryEvent1.map(_.unauthorisedPaymentValue).sum
      Ok(view(form, waypoints, mappedMemberOrEmployer, total))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val total = BigDecimal(0) //request.userAnswers.memberOrEmployerSummaryEvent1.map(_.unauthorisedPaymentValue).sum
          BadRequest(view(formWithErrors, waypoints, Nil, total))
        },
        value => {
          val userAnswerUpdated = UserAnswers().setOrException(UnauthPaymentSummaryPage, value)
          Redirect(UnauthPaymentSummaryPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)}
    )
  }
}
