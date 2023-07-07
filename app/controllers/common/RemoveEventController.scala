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

package controllers.common

import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.common.RemoveEventFormProvider
import models.UserAnswers
import models.enumeration.EventType
import models.requests.DataRequest
import pages.Waypoints
import pages.common.RemoveEventPage
import pages.event18.Event18ConfirmationPage
import play.api.i18n.I18nSupport
import play.api.libs.json.JsPath
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.common.RemoveEventView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveEventController @Inject()(
                                       val controllerComponents: MessagesControllerComponents,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       userAnswersCacheConnector: UserAnswersCacheConnector,
                                       formProvider: RemoveEventFormProvider,
                                       view: RemoveEventView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)
    andThen requireData) { implicit request =>
    val form = formProvider(eventType)
    Ok(view(form, waypoints, eventType))
  }

//  private def update(value: Boolean, eventType: EventType)(implicit request: DataRequest[AnyContent]): Future[UserAnswers] = {
//    val originalUserAnswers = request.userAnswers
//    if (value) {
//      val updatedAnswers = originalUserAnswers.setOrException(Event18ConfirmationPage, false)
//      userAnswersCacheConnector
//        .save(request.pstr, eventType, updatedAnswers)
//        .map(_ => updatedAnswers)
//    }
//    else {
//      Future.successful(originalUserAnswers)
//    }
//  }

  private def remove(value: Boolean, eventType: EventType)(implicit request: DataRequest[AnyContent]): UserAnswers = {
    val originalUserAnswers = request.userAnswers
    if (value) {
      request.userAnswers.removeWithPath(JsPath \ s"event${eventType.toString}")
    }
    else {
      originalUserAnswers
    }
  }

  def onSubmit(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      val form = formProvider(eventType)
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, waypoints, eventType))),
        value => {
          val originalUserAnswers = request.userAnswers
          val updatedUserAnswers = remove(value, eventType)
            userAnswersCacheConnector.save(request.pstr, eventType, updatedUserAnswers).map { _ =>
            Redirect(RemoveEventPage(eventType).navigate(waypoints, originalUserAnswers, updatedUserAnswers).route)
          }
        }
      )
  }
}
