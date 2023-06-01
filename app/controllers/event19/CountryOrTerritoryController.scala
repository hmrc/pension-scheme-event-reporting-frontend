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

package controllers.event19

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.event19.CountryOrTerritoryFormProvider
import models.enumeration.EventType
import pages.Waypoints
import pages.event19.CountryOrTerritoryPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CountryOptions
import views.html.event19.CountryOrTerritoryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountryOrTerritoryController @Inject()(val controllerComponents: MessagesControllerComponents,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             userAnswersCacheConnector: UserAnswersCacheConnector,
                                             formProvider: CountryOrTerritoryFormProvider,
                                             view: CountryOrTerritoryView,
                                             val countryOptions: CountryOptions
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[String] = formProvider()
  private val eventType = EventType.Event19


  def onPageLoad(waypoints: Waypoints): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(CountryOrTerritoryPage).fold(form)(form.fill)
      Ok(
        view(
          preparedForm,
          waypoints,
          countryOptions.options
        )
      )
    }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData).async {
      implicit request =>
        val page = CountryOrTerritoryPage
        form.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  waypoints,
                  countryOptions.options
                )
              )
            )
          },
          value => {
            val originalUserAnswers = request.userAnswers
            val updatedAnswers = originalUserAnswers.setOrException(page, value)
            userAnswersCacheConnector.save(request.pstr, updatedAnswers).map { _ =>
              Redirect(page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
            }
          }
        )
    }

}
