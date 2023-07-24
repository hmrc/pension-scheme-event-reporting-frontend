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
import forms.common.RemoveEventFormProvider
import models.enumeration.EventType
import pages.{VersionInfoPage, Waypoints}
import pages.common.RemoveEventPage
import play.api.i18n.I18nSupport
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
                                       formProvider: RemoveEventFormProvider,
                                       eventReportingConnector: EventReportingConnector,
                                       view: RemoveEventView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)
    andThen requireData) { implicit request =>
    val form = formProvider(eventType)
    Ok(view(form, waypoints, eventType))
  }

  def onSubmit(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      val form = formProvider(eventType)
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, waypoints, eventType))),
        value => {
          val originalUserAnswers = request.userAnswers
          if (value) {
            eventReportingConnector.compileEvent(request.pstr, request.userAnswers.eventDataIdentifier(eventType), currentVersion = request.userAnswers.get(VersionInfoPage).map(_.version).getOrElse(0), delete = true).flatMap { _ =>
              Future.successful(Redirect(RemoveEventPage(eventType).navigate(waypoints, originalUserAnswers, originalUserAnswers).route))
            }
          } else {
            Future.successful(Redirect(RemoveEventPage(eventType).navigate(waypoints, originalUserAnswers, originalUserAnswers).route))
          }
        }
      )
  }
}
