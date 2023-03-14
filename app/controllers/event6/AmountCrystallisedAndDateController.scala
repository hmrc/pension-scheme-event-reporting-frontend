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

package controllers.event6

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import controllers.event6.AmountCrystallisedAndDateController.startOfCrystallisationDate
import forms.event6.AmountCrystallisedAndDateFormProvider
import models.enumeration.EventType
import models.event6.CrystallisedDetails
import models.{Index, Quarters, TaxYear, UserAnswers}
import pages.event6.AmountCrystallisedAndDatePage
import pages.{TaxYearPage, Waypoints}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event6.AmountCrystallisedAndDateView

import java.time.{LocalDate, Month}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmountCrystallisedAndDateController @Inject()(val controllerComponents: MessagesControllerComponents,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    userAnswersCacheConnector: UserAnswersCacheConnector,
                                                    formProvider: AmountCrystallisedAndDateFormProvider,
                                                    view: AmountCrystallisedAndDateView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(startDate: LocalDate)(implicit messages: Messages): Form[CrystallisedDetails] = {
    val endDate = Quarters.getQuarter(startDate).endDate
    formProvider(
      startDate,
      endDate
    )
  }

  private val eventType = EventType.Event6

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    val startDate = startOfCrystallisationDate(request.userAnswers.flatMap(_.get(TaxYearPage)))

    val preparedForm = request.userAnswers.flatMap(_.get(AmountCrystallisedAndDatePage(index))) match {
      case Some(value) => form(startDate = startDate).fill(value)
      case None => form(startDate)
    }
    Ok(view(preparedForm, waypoints, index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)).async { implicit request =>
    val startDate = startOfCrystallisationDate(request.userAnswers.flatMap(_.get(TaxYearPage)))
    form(startDate).bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(view(formWithErrors, waypoints, index)))
      },
      value => {
        val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
        val updatedAnswers = originalUserAnswers.setOrException(AmountCrystallisedAndDatePage(index), value)
        userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(AmountCrystallisedAndDatePage(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}

object AmountCrystallisedAndDateController {
  def startOfCrystallisationDate(optTaxYear: Option[TaxYear]): LocalDate = optTaxYear match {
    case Some(value) =>
      val taxYear = value.startYear
      LocalDate.of(Integer.parseInt(taxYear), Month.APRIL, 6)
    case _ => LocalDate.now()
  }
}
