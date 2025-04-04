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
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.common.TotalPensionAmountsFormProvider
import models.TaxYear.getTaxYearFromOption
import models.common.ChooseTaxYear
import models.{Index, UserAnswers}
import models.enumeration.EventType
import org.apache.commons.lang3.StringUtils
import pages.common.TotalPensionAmountsPage
import pages.{Waypoints, common}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.common.TotalPensionAmountsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TotalPensionAmountsController @Inject()(val controllerComponents: MessagesControllerComponents,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              userAnswersCacheConnector: UserAnswersCacheConnector,
                                              formProvider: TotalPensionAmountsFormProvider,
                                              view: TotalPensionAmountsView
                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] =
    (identify andThen getData(eventType)) { implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.get(TotalPensionAmountsPage(eventType, index))).fold(form)(form.fill)
      val selectedTaxYear = getSelectedTaxYear(request.userAnswers, eventType, index)
      Ok(view(preparedForm, waypoints, eventType, selectedTaxYear, index))
    }

  private def getSelectedTaxYear(userAnswers: Option[UserAnswers], eventType: EventType, index: Index)(implicit messages: Messages): String = {
    val taxYearChosen = getTaxYearFromOption(userAnswers)
    userAnswers.flatMap(_.get(common.ChooseTaxYearPage(eventType, index))(ChooseTaxYear.reads(ChooseTaxYear.enumerable(taxYearChosen)))) match {
      case Some(taxYear) => " " + messages("chooseTaxYear.yearRangeRadio", taxYear.toString, (taxYear.toString.toInt + 1).toString)
      case _ => StringUtils.EMPTY
    }
  }

  def onSubmit(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] =
    (identify andThen getData(eventType)).async {
      implicit request =>
        form.bindFromRequest().fold(
          formWithErrors => {
            val selectedTaxYear = getSelectedTaxYear(request.userAnswers, eventType, index)
            Future.successful(BadRequest(view(formWithErrors, waypoints, eventType, selectedTaxYear, index)))
          },
          value => {
            val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
            val updatedAnswers = originalUserAnswers.setOrException(TotalPensionAmountsPage(eventType, index), value)
            userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
              Redirect(TotalPensionAmountsPage(eventType, index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
            }
          }
        )
    }
}
