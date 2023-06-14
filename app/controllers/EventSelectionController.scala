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

package controllers

import audit.{AuditService, StartNewERAuditEvent}
import connectors.UserAnswersCacheConnector
import controllers.actions.IdentifierAction
import forms.EventSelectionFormProvider
import models.{TaxYear, UserAnswers}
import models.enumeration.EventType
import pages.{EventSelectionPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.EventSelectionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EventSelectionController @Inject()(val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         formProvider: EventSelectionFormProvider,
                                         view: EventSelectionView,
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         auditService: AuditService
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = identify { implicit request =>
    Ok(view(form, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = identify.async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints))),
        value => {
          EventType.fromEventSelection(value) match {
            case Some(eventType) =>
              val futureUA = userAnswersCacheConnector.get(request.pstr, eventType).map {
                case Some(ua) => ua
                case None => UserAnswers()
              }

              futureUA.map { ua =>

                val taxYear = TaxYear.getSelectedTaxYear(ua)
                val answers = ua.setOrException(EventSelectionPage, value)
                auditService.sendEvent(
                  StartNewERAuditEvent(request.loggedInUser.psaIdOrPspId, request.pstr, taxYear = taxYear, eventType: EventType))
                Redirect(EventSelectionPage.navigate(waypoints, answers, answers).route)
              }
            case _ => Future.successful(Ok(view(form, waypoints)))
          }
        }
      )
  }
}
