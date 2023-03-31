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

package controllers.event8

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import controllers.event8.LumpSumAmountAndDateController.endOfLumpSumDate
import forms.event8.LumpSumAmountAndDateFormProvider
import models.enumeration.EventType
import models.event8.LumpSumDetails
import models.{Index, TaxYear, UserAnswers}
import pages.event8.LumpSumAmountAndDatePage
import pages.{TaxYearPage, Waypoints}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event8.LumpSumAmountAndDateView

import java.time.{LocalDate, Month}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LumpSumAmountAndDateController @Inject()(val controllerComponents: MessagesControllerComponents,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               userAnswersCacheConnector: UserAnswersCacheConnector,
                                               formProvider: LumpSumAmountAndDateFormProvider,
                                               view: LumpSumAmountAndDateView
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport { // scalastyle:off magic.number

  private def form(startDate: LocalDate, endDate: LocalDate)(implicit messages: Messages): Form[LumpSumDetails] = {
    formProvider(
      startDate,
      endDate
    )
  }

  def onPageLoad(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    val startDate = LocalDate.of(2006, Month.APRIL, 6)
    val endDate = endOfLumpSumDate(request.userAnswers.flatMap(_.get(TaxYearPage)))

    val preparedForm = request.userAnswers.flatMap(_.get(LumpSumAmountAndDatePage(eventType, index))) match {
      case Some(value) => form(startDate = startDate, endDate).fill(value)
      case None => form(startDate, endDate)
    }
    Ok(view(preparedForm, waypoints, eventType, index))
  }

  def onSubmit(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] = (identify andThen getData(eventType)).async { implicit request =>
    val startDate = LocalDate.of(2006, Month.APRIL, 6)
    val endDate = endOfLumpSumDate(request.userAnswers.flatMap(_.get(TaxYearPage)))

    form(startDate, endDate).bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(view(formWithErrors, waypoints, eventType, index)))
      },
      value => {
        val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
        val updatedAnswers = originalUserAnswers.setOrException(LumpSumAmountAndDatePage(eventType, index), value)
        userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(LumpSumAmountAndDatePage(eventType, index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}

object LumpSumAmountAndDateController {
  private def endOfLumpSumDate(optTaxYear: Option[TaxYear]): LocalDate = optTaxYear match {
    case Some(value) =>
      val taxYear = value.endYear.toInt
      LocalDate.of(taxYear, Month.APRIL, 5)
    case _ => LocalDate.now()
  }
}
