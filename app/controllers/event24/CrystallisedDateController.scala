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

package controllers.event24

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.event24.CrystallisedDateFormProvider
import models.enumeration.EventType
import models.{Index, TaxYear}
import pages.Waypoints
import pages.event24.CrystallisedDatePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event24.CrystallisedDateView

import java.time.{LocalDate, Month}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CrystallisedDateController @Inject()(val controllerComponents: MessagesControllerComponents,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    userAnswersCacheConnector: UserAnswersCacheConnector,
                                                    formProvider: CrystallisedDateFormProvider,
                                                    view: CrystallisedDateView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event24

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    val selectedTaxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers).toInt
    val startDate = LocalDate.of(selectedTaxYear - 1, Month.APRIL, 6)
    val endDate = LocalDate.of(selectedTaxYear, Month.APRIL, 5)

    val preparedForm = request.userAnswers.get(CrystallisedDatePage(index)) match {
      case Some(value) => formProvider(startDate, endDate).fill(value)
      case None => formProvider(startDate, endDate)
    }
    Ok(view(preparedForm, waypoints, index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async { implicit request =>
    val selectedTaxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers).toInt
    val startDate = LocalDate.of(selectedTaxYear - 1, Month.APRIL, 6)
    val endDate = LocalDate.of(selectedTaxYear, Month.APRIL, 5)

    formProvider(startDate, endDate).bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(view(formWithErrors, waypoints, index)))
      },
      value => {
        val originalUserAnswers = request.userAnswers
        val updatedAnswers = originalUserAnswers.setOrException(CrystallisedDatePage(index), value)
        userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(CrystallisedDatePage(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}
