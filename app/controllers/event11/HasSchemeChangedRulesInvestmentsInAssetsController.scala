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

package controllers.event11

import connectors.UserAnswersCacheConnector
import models.enumeration.EventType
import controllers.actions._
import forms.event11.HasSchemeChangedRulesInvestmentsInAssetsFormProvider
import javax.inject.Inject
import pages.Waypoints
import pages.event11.HasSchemeChangedRulesInvestmentsInAssetsPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event11.HasSchemeChangedRulesInvestmentsInAssetsView

import scala.concurrent.{ExecutionContext, Future}

class HasSchemeChangedRulesInvestmentsInAssetsController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: HasSchemeChangedRulesInvestmentsInAssetsFormProvider,
                                        view: HasSchemeChangedRulesInvestmentsInAssetsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event11

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(HasSchemeChangedRulesInvestmentsInAssetsPage).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, waypoints))),
        value => {
          val originalUserAnswers = request.userAnswers
          val updatedAnswers = originalUserAnswers.setOrException(HasSchemeChangedRulesInvestmentsInAssetsPage, value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(HasSchemeChangedRulesInvestmentsInAssetsPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}
