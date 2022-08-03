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

package pages

import models.Mode

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
      CheckYourAnswersPage.event18.urlFragment -> CheckYourAnswersPage.event18.waypoint
    )

  def fromString(s: String): Option[Waypoint] =
    fragments.get(s)
}
