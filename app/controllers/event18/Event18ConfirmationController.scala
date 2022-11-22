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

package controllers.event18

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import models.enumeration.EventType
import pages.Waypoints
import pages.event18.{Event18ConfirmationPage, RemoveEvent18Page}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event18.Event18ConfirmationView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class Event18ConfirmationController @Inject()(val controllerComponents: MessagesControllerComponents,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              userAnswersCacheConnector: UserAnswersCacheConnector,
                                              view: Event18ConfirmationView
                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event18

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    Ok(view(routes.Event18ConfirmationController.onClick(waypoints).url, waypoints))
  }

  def onClick(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
      val updatedAnswers = originalUserAnswers.setOrException(Event18ConfirmationPage, true)
      userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
        Redirect(RemoveEvent18Page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
      }
  }

}
