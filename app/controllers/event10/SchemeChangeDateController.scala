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

package controllers.event10

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.event10.SchemeChangeDateFormProvider
import models.TaxYear
import models.enumeration.EventType
import pages.Waypoints
import pages.event10.{BecomeOrCeaseSchemePage, SchemeChangeDatePage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event10.SchemeChangeDateView

import java.time.{LocalDate, Month}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SchemeChangeDateController @Inject()(val controllerComponents: MessagesControllerComponents,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           userAnswersCacheConnector: UserAnswersCacheConnector,
                                           formProvider: SchemeChangeDateFormProvider,
                                           view: SchemeChangeDateView
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event10

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    val becomeOrCeased = request.userAnswers.get(BecomeOrCeaseSchemePage) match {
      case Some(value) => value.toString
      case _ => throw new RuntimeException("Became or Ceased value unavailable")
    }

    val selectedTaxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers).toInt
    val startDate = LocalDate.of(selectedTaxYear - 1, Month.APRIL, 6)
    val endDate = LocalDate.of(selectedTaxYear, Month.APRIL, 5)

    val preparedForm = request.userAnswers.get(SchemeChangeDatePage) match {
      case Some(value) => formProvider(startDate, endDate).fill(value)
      case None => formProvider(startDate, endDate)
    }

    Ok(view(preparedForm, waypoints, becomeOrCeased))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      val becomeOrCeased = request.userAnswers.get(BecomeOrCeaseSchemePage) match {
        case Some(value) => value.toString
        case _ => throw new RuntimeException("Became or Ceased value unavailable")
      }

      val selectedTaxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers).toInt
      val startDate = LocalDate.of(selectedTaxYear - 1, Month.APRIL, 6)
      val endDate = LocalDate.of(selectedTaxYear, Month.APRIL, 5)

      formProvider(startDate, endDate).bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(view(formWithErrors, waypoints, becomeOrCeased)))
        },
        value => {
          val originalUserAnswers = request.userAnswers
          val updatedAnswers = originalUserAnswers.setOrException(SchemeChangeDatePage, value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(SchemeChangeDatePage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

}
