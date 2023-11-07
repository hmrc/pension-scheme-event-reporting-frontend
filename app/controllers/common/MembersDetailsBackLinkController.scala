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
import models.Index.indexToInt
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.common.MembersDetailsBackLinkPage
import pages.{EmptyWaypoints, Waypoints}
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

  //scalastyle:off
  def onBackClick(waypoints: Waypoints, eventType: EventType, index: Index, memberPageNo: Int): Action[AnyContent] = {
    (identify andThen getData(eventType)) { implicit request =>

      println("\n\n\n\n\n\n\n" + "INSIDE BACK LINK ON PAGE LOAD")
      println("\n\n\n\n\n\n\n" + "CAME FROM A CYA PAGE: " + (waypoints == EmptyWaypoints))
      println("\n\n\n\n\n\n\n" + "EVENT TYPE: " + eventType)
      println("\n\n\n\n\n\n\n" + "INDEX: " + index)
      println("\n\n\n\n\n\n\n" + "MEMBER PAGE NO: " + memberPageNo)

      val page = MembersDetailsBackLinkPage
      val cameFromCYAPage = waypoints != EmptyWaypoints
      val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)

      if (cameFromCYAPage) {
        Redirect(page(eventType, index, memberPageNo).navigate(waypoints, originalUserAnswers, originalUserAnswers).route)
      } else {
        // TODO: remove from UA relevant data for a given event.
        val updatedUserAnswers = ???
        Redirect(page(eventType, index, memberPageNo).navigate(waypoints, originalUserAnswers, updatedUserAnswers).route)
      }
    }
  }
}
