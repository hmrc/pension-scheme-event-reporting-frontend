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

package controllers.event20A

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.event20A.CeasedDateFormProvider
import models.TaxYear
import models.enumeration.EventType
import pages.event20A.{CeasedDatePage, WhatChangePage}
import pages.{JourneyRecoveryPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event20A.CeasedDateView

import java.time.{LocalDate, Month}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CeasedDateController @Inject()(val controllerComponents: MessagesControllerComponents,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     userAnswersCacheConnector: UserAnswersCacheConnector,
                                     formProvider: CeasedDateFormProvider,
                                     view: CeasedDateView
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event20A

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    request.userAnswers.get(WhatChangePage) match {
      case Some(_) =>
        val selectedTaxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers).toInt
        val startDate = LocalDate.of(selectedTaxYear - 1, Month.APRIL, 6)
        val endDate = LocalDate.of(selectedTaxYear, Month.APRIL, 5)

        val preparedForm = request.userAnswers.get(CeasedDatePage) match {
          case Some(value) => formProvider(startDate, endDate).fill(value)
          case None => formProvider(startDate, endDate)
        }
        Ok(view(preparedForm, waypoints))
      case _ => Redirect(JourneyRecoveryPage.route(waypoints))
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      request.userAnswers.get(WhatChangePage) match {
        case Some(_) =>
          val selectedTaxYear = TaxYear.getSelectedTaxYearAsString(request.userAnswers).toInt
          val startDate = LocalDate.of(selectedTaxYear - 1, Month.APRIL, 6)
          val endDate = LocalDate.of(selectedTaxYear, Month.APRIL, 5)

          formProvider(startDate, endDate).bindFromRequest().fold(
            formWithErrors => {
              Future.successful(BadRequest(view(formWithErrors, waypoints)))
            },
            value => {
              val originalUserAnswers = request.userAnswers
              val updatedAnswers = originalUserAnswers.setOrException(CeasedDatePage, value)
              userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
                Redirect(CeasedDatePage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
              }
            }
          )
        case _ => Future.successful(Redirect(JourneyRecoveryPage.route(waypoints)))
      }
  }

}
