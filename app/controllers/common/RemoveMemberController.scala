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
import models.enumeration.EventType
import pages.common.RemoveMemberPage
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

  private def eventTypeMessage(eventType: EventType)(implicit messages: Messages): String = messages(s"eventDescription.event${eventType.toString}")

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

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(
            view(formWithErrors, waypoints, eventType, eventTypeMessage(eventType), index))
          ),
        deleteMember
      )
  }

}
