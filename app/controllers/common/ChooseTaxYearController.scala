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
import forms.common.ChooseTaxYearFormProvider
import models.TaxYear.getTaxYearFromOption
import models.common.ChooseTaxYear
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.{Waypoints, common}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.common.ChooseTaxYearView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChooseTaxYearController @Inject()(val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: ChooseTaxYearFormProvider,
                                        view: ChooseTaxYearView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] =
    (identify andThen getData(eventType)) { implicit request =>
      val taxYearChosen = getTaxYearFromOption(request.userAnswers)
      val form = formProvider(eventType, taxYearChosen)
      val rdsTaxYear = ChooseTaxYear.reads(ChooseTaxYear.enumerable(taxYearChosen))
      val preparedForm = request.userAnswers.flatMap(_.get(common.ChooseTaxYearPage(eventType, index))(rdsTaxYear)).fold(form)(form.fill)
      Ok(view(preparedForm, waypoints, eventType, index, taxYearChosen))
    }

  def onSubmit(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] =
    (identify andThen getData(eventType)).async {
    implicit request =>
      val taxYearChosen = getTaxYearFromOption(request.userAnswers)
      val form = formProvider(eventType, taxYearChosen)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints, eventType, index, taxYearChosen))),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(common.ChooseTaxYearPage(eventType, index), value)(using ChooseTaxYear.writes(using ChooseTaxYear.enumerable(taxYearChosen)))
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(common.ChooseTaxYearPage(eventType, index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }
}
