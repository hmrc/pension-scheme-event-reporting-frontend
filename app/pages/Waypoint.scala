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

package pages

import models.Mode
import pages.event1.Event1CheckYourAnswersPage
import pages.event10.Event10CheckYourAnswersPage
import pages.event11.Event11CheckYourAnswersPage
import pages.event12.Event12CheckYourAnswersPage
import pages.event13.Event13CheckYourAnswersPage
import pages.event14.Event14CheckYourAnswersPage
import pages.event19.Event19CheckYourAnswersPage
import pages.event2.Event2CheckYourAnswersPage
import pages.event20.Event20CheckYourAnswersPage
import pages.event20A.Event20ACheckYourAnswersPage
import pages.event22.Event22CheckYourAnswersPage
import pages.event23.Event23CheckYourAnswersPage
import pages.event24.Event24CheckYourAnswersPage
import pages.event3.Event3CheckYourAnswersPage
import pages.event4.Event4CheckYourAnswersPage
import pages.event5.Event5CheckYourAnswersPage
import pages.event6.Event6CheckYourAnswersPage
import pages.event7.Event7CheckYourAnswersPage
import pages.event8.Event8CheckYourAnswersPage
import pages.event8a.Event8ACheckYourAnswersPage
import pages.eventWindUp.EventWindUpCheckYourAnswersPage

case class Waypoint(
                     page: WaypointPage,
                     mode: Mode,
                     urlFragment: String
                   )

object Waypoint {

  /*
  Every CYA page you create needs to extend CheckAnswersPage and will get a url fragment; and every add-to-list page will extend
  AddToListPage and get normal-mode and check-mode fragments.  And yes, you’ll add them to the fromString method on Waypoint as well.
  See claim-child-benefit-frontend project for examples of how this is used.
   */

  private val fragments: Map[String, Waypoint] =
    Map(
      Event11CheckYourAnswersPage().urlFragment -> Event11CheckYourAnswersPage().waypoint,
      Event14CheckYourAnswersPage.urlFragment -> Event14CheckYourAnswersPage.waypoint,
      Event19CheckYourAnswersPage.urlFragment -> Event19CheckYourAnswersPage.waypoint,
      Event20CheckYourAnswersPage().urlFragment -> Event20CheckYourAnswersPage().waypoint,
      Event20ACheckYourAnswersPage().urlFragment -> Event20ACheckYourAnswersPage().waypoint,
      EventWindUpCheckYourAnswersPage.urlFragment -> EventWindUpCheckYourAnswersPage.waypoint
    )

  /*
  All CYA page objects which have an index should be added below. Those without an index should be added above.
   */

  def fromString(s: String): Option[Waypoint] =
    fragments.get(s)
      .orElse(Event1CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event2CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event3CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event4CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event5CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event6CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event7CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event8CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event8ACheckYourAnswersPage.waypointFromString(s))
      .orElse(Event10CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event12CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event13CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event22CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event23CheckYourAnswersPage.waypointFromString(s))
      .orElse(Event24CheckYourAnswersPage.waypointFromString(s))

}
