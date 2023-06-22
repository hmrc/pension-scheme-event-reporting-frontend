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

package controllers.event20A

import connectors.{MinimalConnector, SchemeDetailsConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event20A.Event20APspDeclarationFormProvider
import helpers.DateHelper.{getTaxYear, getTaxYearFromOption}
import models.UserAnswers
import models.enumeration.EventType
import pages.Waypoints
import pages.event20A.Event20APspDeclarationPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event20A.Event20APspDeclarationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event20APspDeclarationController @Inject()(val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         formProvider: Event20APspDeclarationFormProvider,
                                         minimalConnector: MinimalConnector,
                                         schemeDetailsConnector: SchemeDetailsConnector,
                                         view: Event20APspDeclarationView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(authorisingPsaId: Option[String]):Form[String] = formProvider(authorisingPSAID = authorisingPsaId)
  private val eventType = EventType.Event1

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(Event20APspDeclarationPage)) match {
      case Some(value) => form(authorisingPsaId=None).fill(value)
      case None => form(authorisingPsaId=None)
    }
    minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).map {
      minimalDetails =>
        Ok(view(request.schemeName, request.pstr, getTaxYearFromOption(request.userAnswers).toString, minimalDetails.name, preparedForm, waypoints))
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).flatMap { minimalDetails =>
        schemeDetailsConnector.getPspSchemeDetails(request.loggedInUser.psaIdOrPspId, request.pstr).map(_.authorisingPSAID).flatMap { authorisingPsaId =>
          form(authorisingPsaId = authorisingPsaId)
            .bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(request.schemeName, request.pstr, getTaxYearFromOption(request.userAnswers).toString, minimalDetails.name, formWithErrors, waypoints))),
            value => {
              val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
              val updatedAnswers = originalUserAnswers.setOrException(Event20APspDeclarationPage, value)
              userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
                Redirect(Event20APspDeclarationPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
              }
            }
          )
        }
      }
  }

}
