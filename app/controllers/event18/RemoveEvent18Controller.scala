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
import models.enumeration.EventType
import controllers.actions._
import forms.event18.RemoveEvent18FormProvider
import models.UserAnswers
import models.requests.DataRequest

import javax.inject.Inject
import pages.Waypoints
import pages.event18.Event18ConfirmationPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event18.RemoveEvent18View

import scala.concurrent.{ExecutionContext, Future}

class RemoveEvent18Controller @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: RemoveEvent18FormProvider,
                                        view: RemoveEvent18View
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event18

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    Ok(view(form, waypoints))
  }

  private def x(implicit request: DataRequest[AnyContent]) = {
    val originalUserAnswers = request.userAnswers
    val updatedAnswers = originalUserAnswers.setOrException(Event18ConfirmationPage, false)
    userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers)
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, waypoints))),
        value => {
          for {
            _ <- x
            updatedAnswers <- y
          } yield {

          }
//          val originalUserAnswers = request.userAnswers
//          val result = if (value) {
//            val updatedAnswers = originalUserAnswers.setOrException(Event18ConfirmationPage, false)
//            userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map(_ => updatedAnswers)
//          }
//          else { Future.successful(originalUserAnswers)
//          }
//          result.map{ userAnswers =>
//          Redirect(Event18ConfirmationPage.navigate(waypoints, originalUserAnswers, userAnswers).route)
//        }
      }
    )
  }
}
