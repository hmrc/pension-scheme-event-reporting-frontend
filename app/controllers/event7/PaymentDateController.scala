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

package controllers.event7

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import controllers.event7.PaymentDateController.paymentDateOpt
import forms.event7.PaymentDateFormProvider
import models.enumeration.EventType
import models.event7.PaymentDate
import models.{Index, Quarters, TaxYear, UserAnswers}
import pages.event7.PaymentDatePage
import pages.{TaxYearPage, Waypoints}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event7.PaymentDateView

import java.time.{LocalDate, Month}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PaymentDateController @Inject()(val controllerComponents: MessagesControllerComponents,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    userAnswersCacheConnector: UserAnswersCacheConnector,
                                                    formProvider: PaymentDateFormProvider,
                                                    view: PaymentDateView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(startDate: LocalDate)(implicit messages: Messages): Form[PaymentDate] = {
    val endDate = Quarters.getQuarter(startDate).endDate
    formProvider(
      endDate
    )
  }

  private val eventType = EventType.Event7

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    val startDate = paymentDateOpt(request.userAnswers.flatMap(_.get(TaxYearPage)))

    val preparedForm = request.userAnswers.flatMap(_.get(PaymentDatePage(index))) match {
      case Some(value) => form(startDate = startDate).fill(value)
      case None => form(startDate)
    }
    Ok(view(preparedForm, waypoints, index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)).async { implicit request =>
    val startDate = paymentDateOpt(request.userAnswers.flatMap(_.get(TaxYearPage)))
    form(startDate).bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(view(formWithErrors, waypoints, index)))
      },
      value => {
        val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
        val updatedAnswers = originalUserAnswers.setOrException(PaymentDatePage(index), value)
        userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(PaymentDatePage(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}

object PaymentDateController {
  def paymentDateOpt(optTaxYear: Option[TaxYear]): LocalDate = optTaxYear match {
    case Some(value) =>
      val taxYear = value.startYear
      LocalDate.of(Integer.parseInt(taxYear), Month.APRIL, 6)
    case _ => LocalDate.now()
  }
}
