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

package controllers.event25

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.event25.CrystallisedDateController.paymentDateOpt
import forms.event25.CrystallisedDateFormProvider
import models.enumeration.EventType
import models.event25.CrystallisedDate
import models.{Index, Quarters, TaxYear, UserAnswers}
import pages.event25.CrystallisedDatePage
import pages.{TaxYearPage, Waypoints}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.time.{LocalDate, Month}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import views.html.event25.CrystallisedDateView

class CrystallisedDateController @Inject()(val controllerComponents: MessagesControllerComponents,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                                    userAnswersCacheConnector: UserAnswersCacheConnector,
                                                    formProvider: CrystallisedDateFormProvider,
                                                    view: CrystallisedDateView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(startDate: LocalDate)(implicit messages: Messages): Form[CrystallisedDate] = {
    val endDate = Quarters.getQuarter(startDate).endDate
    formProvider(
      endDate
    )
  }

  private val eventType = EventType.Event7

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    val startDate = paymentDateOpt(request.userAnswers.get(TaxYearPage))

    val preparedForm = request.userAnswers.get(CrystallisedDatePage(index)) match {
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
        val updatedAnswers = originalUserAnswers.setOrException(CrystallisedDatePage(index), value)
        userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(CrystallisedDatePage(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}

object CrystallisedDateController {
  def paymentDateOpt(optTaxYear: Option[TaxYear]): LocalDate = optTaxYear match {
    case Some(value) =>
      val taxYear = value.startYear
      LocalDate.of(Integer.parseInt(taxYear), Month.APRIL, 6)
    case _ => LocalDate.now()
  }
}
