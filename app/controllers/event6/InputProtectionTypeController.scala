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

package controllers.event6

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event6.InputProtectionTypeFormProvider
import models.{Index, UserAnswers}
import models.enumeration.EventType
import org.apache.commons.lang3.StringUtils
import pages.Waypoints
import pages.event6.{InputProtectionTypePage, TypeOfProtectionPage}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event6.InputProtectionTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InputProtectionTypeController @Inject()(val controllerComponents: MessagesControllerComponents,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              userAnswersCacheConnector: UserAnswersCacheConnector,
                                              formProvider: InputProtectionTypeFormProvider,
                                              view: InputProtectionTypeView
                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event6
  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    val protectionTypeDesc = getProtectionTypeDesc(request.userAnswers, index)
    val preparedForm = request.userAnswers.flatMap(_.get(InputProtectionTypePage(eventType, index))).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints, index, protectionTypeDesc))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      val protectionTypeDesc = getProtectionTypeDesc(request.userAnswers, index)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints, index, protectionTypeDesc))),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(InputProtectionTypePage(eventType, index), value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(InputProtectionTypePage(eventType, index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

  private def getProtectionTypeDesc(userAnswers: Option[UserAnswers], index: Index)(implicit messages: Messages): String = {
    userAnswers.flatMap(_.get(TypeOfProtectionPage(eventType, index))) match {
      case Some(typeOfProtection) => messages(s"typeOfProtection.${typeOfProtection.toString}").toLowerCase
      case _ => StringUtils.EMPTY
    }
  }
}