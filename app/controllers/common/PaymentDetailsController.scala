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

package controllers.common

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.common.PaymentDetailsFormProvider
import models.enumeration.EventType
import models.{Index, TaxYear}
import pages.Waypoints
import pages.common.PaymentDetailsPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.common.PaymentDetailsView

import java.time.{LocalDate, Month}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PaymentDetailsController @Inject()(val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         formProvider: PaymentDetailsFormProvider,
                                         view: PaymentDetailsView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport { // scalastyle:off magic.number

  private def taxYearToLocalDate(taxYear: String): LocalDate = {
    LocalDate.of(taxYear.toInt, Month.APRIL, 5)
  }

  def onPageLoad(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    val startDate = LocalDate.of(2006, Month.APRIL, 6)
    val endDate = taxYearToLocalDate(TaxYear.getSelectedTaxYearAsString(request.userAnswers))

    val preparedForm = request.userAnswers.get(PaymentDetailsPage(eventType, index)) match {
      case Some(value) => formProvider(startDate, endDate).fill(value)
      case None => formProvider(startDate, endDate)
    }
    Ok(view(preparedForm, waypoints, eventType, index, startDate, endDate))
  }

  def onSubmit(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async { implicit request =>
    val startDate = LocalDate.of(2006, Month.APRIL, 6)
    val endDate = taxYearToLocalDate(TaxYear.getSelectedTaxYearAsString(request.userAnswers))

    formProvider(startDate, endDate).bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(view(formWithErrors, waypoints, eventType, index, startDate, endDate)))
      },
      value => {
        val originalUserAnswers = request.userAnswers
        val updatedAnswers = originalUserAnswers.setOrException(PaymentDetailsPage(eventType, index), value)
        userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(PaymentDetailsPage(eventType, index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}