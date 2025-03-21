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

package controllers.event1

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event1.PaymentValueAndDateFormProvider
import models.TaxYear.getTaxYearFromOption
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.Waypoints
import pages.event1.PaymentValueAndDatePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event1.PaymentValueAndDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PaymentValueAndDateController @Inject()(val controllerComponents: MessagesControllerComponents,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              userAnswersCacheConnector: UserAnswersCacheConnector,
                                              formProvider: PaymentValueAndDateFormProvider,
                                              view: PaymentValueAndDateView
                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  private val eventType = EventType.Event1

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    val taxYear = getTaxYearFromOption(request.userAnswers)
    val form = formProvider(taxYear)
    val preparedForm = request.userAnswers.flatMap(_.get(PaymentValueAndDatePage(index))).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints, index, taxYear))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)).async { implicit request =>
    val taxYear = getTaxYearFromOption(request.userAnswers)
    val form = formProvider(taxYear)
    form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(view(formWithErrors, waypoints, index, taxYear)))
      },

      value => {
        val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
        val updatedAnswers = originalUserAnswers.setOrException(PaymentValueAndDatePage(index), value)
        userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(PaymentValueAndDatePage(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}
