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

package controllers.event14

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event14.HowManySchemeMembersFormProvider
import models.TaxYear.getSelectedTaxYearAsString
import models.UserAnswers
import models.enumeration.EventType
import models.event14.HowManySchemeMembers
import pages.Waypoints
import pages.event14.HowManySchemeMembersPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event14.HowManySchemeMembersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HowManySchemeMembersController @Inject()(val controllerComponents: MessagesControllerComponents,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               userAnswersCacheConnector: UserAnswersCacheConnector,
                                               formProvider: HowManySchemeMembersFormProvider,
                                               view: HowManySchemeMembersView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event14

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    val taxYearEnd = getSelectedTaxYearAsString(request.userAnswers.get)
    val taxYearRange = s"${taxYearEnd.toInt - 1} to $taxYearEnd"
    def form(implicit messages: Messages): Form[HowManySchemeMembers] =
      formProvider(messages("howManySchemeMembers.error.required", taxYearRange))
    val preparedForm = request.userAnswers.flatMap(_.get(HowManySchemeMembersPage)).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints, taxYearRange))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      val taxYearEnd = getSelectedTaxYearAsString(request.userAnswers.get)
      val taxYearRange = s"${taxYearEnd.toInt - 1} to $taxYearEnd"
      def form(implicit messages: Messages): Form[HowManySchemeMembers] =
        formProvider(messages("howManySchemeMembers.error.required", taxYearRange))
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints, taxYearRange))),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(HowManySchemeMembersPage, value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(HowManySchemeMembersPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

}
