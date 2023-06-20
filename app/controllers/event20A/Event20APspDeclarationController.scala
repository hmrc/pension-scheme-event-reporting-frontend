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

package controllers.event20A

import connectors.{MinimalConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event20A.Event20APspDeclarationFormProvider
import helpers.DateHelper.getTaxYear
import models.UserAnswers
import models.enumeration.EventType
import pages.Waypoints
import pages.event20A.Event20APspDeclarationPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event20A.Event20APspDeclarationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event20APspDeclarationController @Inject()(val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         formProvider: Event20APspDeclarationFormProvider,
                                         minimalConnector: MinimalConnector,
                                         view: Event20APspDeclarationView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event1

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(Event20APspDeclarationPage)).fold(form)(form.fill)
    minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).map {
      minimalDetails =>
        Ok(view(request.schemeName, request.pstr, getTaxYear(request.userAnswers).toString, minimalDetails.name, preparedForm, waypoints))
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).flatMap{ minimalDetails =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(request.schemeName, request.pstr, getTaxYear(request.userAnswers).toString, minimalDetails.name, formWithErrors, waypoints))),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(Event20APspDeclarationPage, value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(Event20APspDeclarationPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
      }
  }

}
