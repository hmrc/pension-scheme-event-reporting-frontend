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
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.Index
import models.Index.indexToInt
import models.enumeration.EventType
import models.enumeration.EventType.Event3
import pages.Waypoints
import pages.common.MembersDetailsPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class MembersDetailsBackLinkController @Inject()(val controllerComponents: MessagesControllerComponents,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                userAnswersCacheConnector: UserAnswersCacheConnector
                                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, eventType: EventType, index: Index, memberPageNo: Int): Action[AnyContent] =
    (identify andThen getData(eventType)) { implicit request =>

      println("\n\n\n\n\n\n\n" + "INSIDE BACK LINK ON PAGE LOAD")
      println("\n\n\n\n\n\n\n" + "WAYPOINTS" + waypoints)
      println("\n\n\n\n\n\n\n" + "EVENT TYPE" + eventType)
      println("\n\n\n\n\n\n\n" + "INDEX" + index)
      println("\n\n\n\n\n\n\n" + "MEMBER PAGE NO" + memberPageNo)

      /* TODO write expected behaviour code.

          val form = formProvider(eventType, memberNinos(eventType, indexToInt(index)), memberPageNo)
      form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, waypoints, eventType, memberPageNo, postCall))),
      value => {
        val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
        val updatedAnswers = originalUserAnswers.setOrException(page, value)
        userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
      )
       */

      Redirect(controllers.routes.EventSelectionController.onPageLoad(waypoints))
    }


  // (waypoints: Waypoints, eventType: EventType, index: Index, memberPageNo: Int):
  def onBackClick(waypoints: Waypoints, eventType: EventType, index: Index, memberPageNo: Int): Action[AnyContent] = {


    (identify andThen getData(eventType)) { implicit request =>
      Redirect(controllers.common.routes.
        MembersDetailsBackLinkController.onPageLoad(waypoints, index))
    }
  }
}
