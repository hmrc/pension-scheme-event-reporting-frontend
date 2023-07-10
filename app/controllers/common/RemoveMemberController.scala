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
import models.enumeration.EventType
import controllers.actions._
import controllers.common.RemoveMemberController.eventTypeMessage
import forms.common.RemoveMemberFormProvider
import models.enumeration.EventType._
import javax.inject.Inject
import pages.Waypoints
import pages.common.RemoveMemberPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.common.RemoveMemberView

import scala.concurrent.{ExecutionContext, Future}

class RemoveMemberController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: RemoveMemberFormProvider,
                                        view: RemoveMemberView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(RemoveMemberPage(eventType)).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints, eventType, eventTypeMessage(eventType)))
  }

  def onSubmit(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, waypoints, eventType, eventTypeMessage(eventType)))),
        value => {
          val originalUserAnswers = request.userAnswers
          val updatedAnswers = originalUserAnswers.setOrException(RemoveMemberPage(eventType), value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(RemoveMemberPage(eventType).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}

object RemoveMemberController {
  def eventTypeMessage(eventType: EventType): String = { eventType match {
    case Event1 => "unauthorised payment"
    case Event2 => "lump sum death benefit payment"
    case Event3 => "benefits provided before normal minimum pension age"
    case Event4 => "payment of serious ill-health lump sum"
    case Event5 => "cessation of ill-health pension"
    case Event6 => "benefit crystallisation event"
    case Event7 | Event8 | Event8A => "pension commencement lump sum"
    case Event22 => "annual allowance"
    case Event23 => "dual annual allowance"
  }
  }
}
