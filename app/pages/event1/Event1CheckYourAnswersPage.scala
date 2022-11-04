/*
 * Copyright 2022 HM Revenue & Customs
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

package pages.event1

import models.Index
import models.enumeration.EventType
import models.enumeration.EventType.Event1
import pages.{CheckAnswersPage, Waypoint, Waypoints}
import play.api.mvc.Call

case class Event1CheckYourAnswersPage(index: Index) extends CheckAnswersPage {

  override val urlFragment: String =
      s"event-${Event1.toString}-check-answers-${index.display}"

  override def route(waypoints: Waypoints): Call = {
      controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(index)
    }

  override def toString: String = "CheckYourAnswersPage"

}

object Event1CheckYourAnswersPage {

  def waypointFromString(s: String): Option[Waypoint] = {
    val pattern = """event-1-check-answers-(\d{1,6})""".r.anchored

    s match {
      case pattern(indexDisplay) =>
        Some(Event1CheckYourAnswersPage(Index(indexDisplay.toInt - 1)).waypoint)
      case _ =>
        None
    }
  }
}


