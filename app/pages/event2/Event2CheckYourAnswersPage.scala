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

package pages.event2

import models.Index
import models.enumeration.EventType.Event2
import pages.{CheckAnswersPage, Waypoint, Waypoints}
import play.api.mvc.Call

case class Event2CheckYourAnswersPage(index: Index) extends CheckAnswersPage {
  override val urlFragment: String =
    s"event-${Event2.toString}-check-answers-${index.display}"

  override def route(waypoints: Waypoints): Call = {
    controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(index)
  }

  override def toString: String = "CheckYourAnswersPage"
}

object Event2CheckYourAnswersPage {

  def waypointFromString(s: String): Option[Waypoint] = {
    val pattern = """event-2-check-answers-(\d{1,6})""".r.anchored

    s match {
      case pattern(indexDisplay) =>
        Some(Event2CheckYourAnswersPage(Index(indexDisplay.toInt - 1)).waypoint)
      case _ =>
        None
    }
  }
}


