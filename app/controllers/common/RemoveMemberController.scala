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

package controllers.common

import controllers.actions._
import forms.common.RemoveMemberFormProvider
import models.Index
import models.enumeration.{EventType, MessageType}
import models.enumeration.EventType.{Event2, Event7, Event8}
import models.requests.DataRequest
import pages.common.{MembersDetailsPage, RemoveMemberPage}
import pages.{VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CompileService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.common.RemoveMemberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveMemberController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        compileService: CompileService,
                                        formProvider: RemoveMemberFormProvider,
                                        view: RemoveMemberView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  private def getMessage(eventType: EventType, memberName: String, messageType: MessageType)(implicit messages: Messages): String = {
    eventType match {
      case Event7 | Event8 =>
        val messagePart1 = messages(s"eventDescription.event${eventType.toString}.part1")
        val messagePart2 = messages(s"eventDescription.event${eventType.toString}.part2")
        messageType match {
          case MessageType.Title => messages("removeMember.commencementLumpSum.title", messagePart1, memberName, messagePart2)
          case MessageType.Heading => messages("removeMember.commencementLumpSum.heading", messagePart1, memberName, messagePart2)
          case MessageType.Error => messages("removeMember.commencementLumpSum.error.required", messagePart1, memberName, messagePart2)
        }
      case _ =>
        val eventTypeMessage = messages(s"eventDescription.event${eventType.toString}")
        messageType match {
          case MessageType.Title => messages("removeMember.title", eventTypeMessage, memberName)
          case MessageType.Heading => messages("removeMember.heading", eventTypeMessage, memberName)
          case MessageType.Error => messages("removeMember.error.required", eventTypeMessage, memberName)
        }
    }
  }
  private def getUserName(request: DataRequest[AnyContent], eventType: EventType, index: Index) = {
    val membersDetails = eventType match {
      case Event2 => request.userAnswers.get(MembersDetailsPage(eventType, index, 1))
      case _ => request.userAnswers.get(MembersDetailsPage(eventType, index))
    }
    membersDetails match {
      case Some(details) => details.fullName
      case _ => "this member"
    }
  }

  def onPageLoad(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>
      val memberName = getUserName(request, eventType, index)
      val errorMessage = getMessage(eventType, memberName, MessageType.Error)
      val form = formProvider(errorMessage)
      val preparedForm = request.userAnswers.get(RemoveMemberPage(eventType, index)).fold(form)(form.fill)
      val title = getMessage(eventType, memberName, MessageType.Title)
      val heading = getMessage(eventType, memberName, MessageType.Heading)
      Ok(view(preparedForm, waypoints, eventType, title, heading, index))
    }

  def onSubmit(waypoints: Waypoints, eventType: EventType, index: Index): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      val memberName = getUserName(request, eventType, index)
      val errorMessage = getMessage(eventType, memberName, MessageType.Error)

      val form = formProvider(errorMessage)

      def deleteMember(delete:Boolean) = {
        val ua = if (delete) {
          val vi = request.userAnswers.get(VersionInfoPage)
          val version = vi.map(_.version).getOrElse(0)
          compileService.deleteMember(
              request.pstr,
              request.userAnswers.eventDataIdentifier(eventType),
              version,
              index.id.toString,
              request.userAnswers
            ).map(_ => request.userAnswers)
        } else {
          Future.successful(request.userAnswers)
        }

        ua.map { ua =>
          Redirect(RemoveMemberPage(eventType, index).navigate(waypoints, ua, ua).route)
        }
      }

      val title = getMessage(eventType, memberName, MessageType.Title)
      val heading = getMessage(eventType, memberName, MessageType.Heading)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(
            view(formWithErrors, waypoints, eventType, title, heading, index))
          ),
        deleteMember
      )
  }

}
