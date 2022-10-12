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
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event1.PaymentValueAndDateFormProvider
import models.{Quarters, UserAnswers}
import models.enumeration.EventType
import models.event1.PaymentDetails
import pages.Waypoints
import pages.event1.PaymentValueAndDatePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event1.PaymentValueAndDateView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PaymentValueAndDateController @Inject()(val controllerComponents: MessagesControllerComponents,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      userAnswersCacheConnector: UserAnswersCacheConnector,
                                      formProvider: PaymentValueAndDateFormProvider,
                                      view: PaymentValueAndDateView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(startDate: LocalDate)(implicit messages: Messages): Form[PaymentDetails] = {
    val endDate = Quarters.getQuarter(startDate).endDate
    formProvider(
      startDate,
      endDate
    )
  }


  private val eventType = EventType.Event1

  // TODO: change implementation to real date once preceding pages are implemented, using stubDate for now.
  private val stubDate: LocalDate = LocalDate.now()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(PaymentValueAndDatePage)) match {
      case Some(value) => form(startDate = stubDate).fill(value)
      case None => form(stubDate)
    }
    Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async { implicit request =>
      form(stubDate).bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(view(formWithErrors, waypoints)))
        },

        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(PaymentValueAndDatePage, value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(PaymentValueAndDatePage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }
}
