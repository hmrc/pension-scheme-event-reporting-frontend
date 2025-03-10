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

package controllers.event24

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.event24.TypeOfProtectionGroup1FormProvider
import models.enumeration.EventType
import models.event24.TypeOfProtectionGroup1
import models.{Index, UserAnswers}
import pages.Waypoints
import pages.event24.{TypeOfProtectionGroup1Page, TypeOfProtectionGroup2Page}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event24.TypeOfProtectionGroup1View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class TypeOfProtectionGroup1Controller @Inject()(
                                            val controllerComponents: MessagesControllerComponents,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            view: TypeOfProtectionGroup1View,
                                            formProvider: TypeOfProtectionGroup1FormProvider,
                                            userAnswersCacheConnector: UserAnswersCacheConnector
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport{

  val form = formProvider()
  private val eventType = EventType.Event24

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(TypeOfProtectionGroup1Page(index)).fold(form)(form.fill)
      val protectionPageVal = request.userAnswers.get(TypeOfProtectionGroup2Page(index)).getOrElse("")
      Ok(view(preparedForm, protectionPageVal.toString, waypoints, index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val protectionPageVal = request.userAnswers.fold(UserAnswers())(identity).get(TypeOfProtectionGroup2Page(index)).getOrElse("")
          Future.successful(BadRequest(view(formWithErrors, protectionPageVal.toString, waypoints, index)))
        },
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(TypeOfProtectionGroup1Page(index), value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(TypeOfProtectionGroup1Page(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }
}
