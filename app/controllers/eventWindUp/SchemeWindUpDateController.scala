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

package controllers.eventWindUp

import connectors.{SchemeConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.eventWindUp.SchemeWindUpDateFormProvider
import helpers.DateHelper
import models.TaxYear.getTaxYearFromOption
import models.UserAnswers
import models.enumeration.EventType
import pages.Waypoints
import pages.eventWindUp.SchemeWindUpDatePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateHelper.formatDateDMY
import views.html.eventWindUp.SchemeWindUpDateView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SchemeWindUpDateController @Inject()(val controllerComponents: MessagesControllerComponents,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    userAnswersCacheConnector: UserAnswersCacheConnector,
                                    schemeConnector : SchemeConnector,
                                    formProvider: SchemeWindUpDateFormProvider,
                                    view: SchemeWindUpDateView,
                                    dateHelper: DateHelper
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.WindUp
  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    //TODO add openDate
    def form: Form[LocalDate] = formProvider(getTaxYearFromOption(request.userAnswers), LocalDate.of(2023,5,1))
    val preparedForm = request.userAnswers.flatMap(_.get(SchemeWindUpDatePage)).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      val psaOrPspId = request.loggedInUser.psaIdOrPspId
      val idValue = request.loggedInUser.idName
            schemeConnector.getOpenDate(idValue, psaOrPspId, request.pstr).flatMap {
              openDate =>
                //TODO add real openDate
                println(s"\n\n\n OPENDATE ===== $openDate\n\n")
                println(s"\n\n\n OPENDATE parsed ===== ${formatDateDMY(openDate)}\n\n")
      def form: Form[LocalDate] = formProvider(getTaxYearFromOption(request.userAnswers), LocalDate.of(2023,5,1))
      form.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(view(formWithErrors, waypoints)))
        },
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(SchemeWindUpDatePage, value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(SchemeWindUpDatePage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

  }

}
