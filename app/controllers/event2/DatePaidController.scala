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

package controllers.event2

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event2.DatePaidFormProvider
import models.TaxYear.getTaxYearFromOption
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.Waypoints
import pages.event2.DatePaidPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.BeneficiaryDetailsEvent2.getBeneficiaryName
import views.html.event2.DatePaidView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DatePaidController @Inject()(val controllerComponents: MessagesControllerComponents,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalAction,
                                   userAnswersCacheConnector: UserAnswersCacheConnector,
                                   formProvider: DatePaidFormProvider,
                                   view: DatePaidView
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event2

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    val form = formProvider(getTaxYearFromOption(request.userAnswers))
    val preparedForm = request.userAnswers.flatMap(_.get(DatePaidPage(index, eventType))).fold(form)(form.fill)

    Ok(view(preparedForm, waypoints, getBeneficiaryName(request.userAnswers, index), index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      val form = formProvider(getTaxYearFromOption(request.userAnswers))
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints, getBeneficiaryName(request.userAnswers, index), index))),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(DatePaidPage(index, eventType), value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(DatePaidPage(index, eventType).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

}
