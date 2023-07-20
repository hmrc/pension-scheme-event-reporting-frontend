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

import connectors.EventReportingConnector
import controllers.actions._
import controllers.common.RemoveMemberController.eventTypeMessage
import forms.common.RemoveMemberFormProvider
import models.Index
import models.enumeration.EventType
import models.enumeration.EventType._
import pages.Waypoints
import pages.common.RemoveMemberPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.common.RemoveMemberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveMemberController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        eventReportingConnector: EventReportingConnector,
                                        formProvider: RemoveMemberFormProvider,
                                        view: RemoveMemberView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>

      val form = formProvider(eventTypeMessage(eventType))
      val preparedForm = request.userAnswers.get(RemoveMemberPage(eventType, index)).fold(form)(form.fill)
      Ok(view(preparedForm, waypoints, eventType, eventTypeMessage(eventType), index))
    }

  def onSubmit(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      val form = formProvider(eventTypeMessage(eventType))

      def deleteMember(delete:Boolean) = {
        val ua = if (delete) {
          eventReportingConnector.deleteMember(request.pstr, request.userAnswers.eventDataIdentifier(eventType), index.id.toString)
            .map(_ => request.userAnswers)
        } else {
          Future.successful(request.userAnswers)
        }

        ua.map { ua =>
          Redirect(RemoveMemberPage(eventType, index).navigate(waypoints, ua, ua).route)
        }
      }

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(
            view(formWithErrors, waypoints, eventType, eventTypeMessage(eventType), index))
          ),
        deleteMember
      )
  }

}

object RemoveMemberController {
  def eventTypeMessage(eventType: EventType): String = {
    //TODO: Replace with messages. - Pavel Vjalicin
    eventType match {
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
