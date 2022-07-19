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
import forms.TestFormProvider
import models.UserAnswers
import models.enumeration.EventType
import pages.{TestPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TestView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestController @Inject()(
                                val controllerComponents: MessagesControllerComponents,
                                identify: IdentifierAction,
                                getData: DataRetrievalAction,
                                userAnswersCacheConnector: UserAnswersCacheConnector,
                                formProvider: TestFormProvider,
                                view: TestView
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  // TODO: This will need to be retrieved from a Mongo collection. Can't put it in URL for security reasons.
  private val pstr = "123"

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(pstr, EventType.Event1)) { implicit request =>
    val preparedForm = request.userAnswers match {
      case None => form
      case Some(ua) => ua.get(TestPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
    }
    Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(pstr, EventType.Event1)).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints))),
        value => {

          val originalUserAnswers = request.userAnswers match {
            case None => UserAnswers()
            case Some(ua) => ua
          }

          val updatedAnswers = originalUserAnswers.setOrException(TestPage, value)
          userAnswersCacheConnector.save(pstr, EventType.Event1, updatedAnswers).map { _ =>
            Redirect(TestPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

}
