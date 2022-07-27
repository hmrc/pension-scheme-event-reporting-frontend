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

package controllers

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.eventSelectionFormProvider
import models.UserAnswers
import models.enumeration.EventType
import pages.{eventSelectionPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.eventSelectionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class eventSelectionController @Inject()(val controllerComponents: MessagesControllerComponents,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          userAnswersCacheConnector: UserAnswersCacheConnector,
                                          formProvider: eventSelectionFormProvider,
                                          view: eventSelectionView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event1

  // TODO: This will need to be retrieved from a Mongo collection. Can't put it in URL for security reasons.
  private val pstr = "123"

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(pstr, eventType)) { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(eventSelectionPage)).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(pstr, eventType)).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints))),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(eventSelectionPage, value)
          userAnswersCacheConnector.save(pstr, eventType, updatedAnswers).map { _ =>
            Redirect(eventSelectionPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

}
