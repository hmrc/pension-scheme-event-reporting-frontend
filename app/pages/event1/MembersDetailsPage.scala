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

import controllers.event1.routes
import models.{CheckMode, UserAnswers}
import models.event1.MembersDetails
import pages.{AddItemPage, CheckAnswersPage, Page, PageAndWaypoints, QuestionPage, WaypointPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object MembersDetailsPage extends QuestionPage[MembersDetails] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "membersDetails"

  override def route(waypoints: Waypoints): Call =
    routes.MembersDetailsController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    DoYouHoldSignedMandatePage

  override def changeLink(waypoints: Waypoints, sourcePage: WaypointPage): PageAndWaypoints = {
    println("\n>>>>CYALLL" + sourcePage)
    sourcePage match {
      case p: CheckAnswersPage =>
        println("\n>>>>CYA" + p.waypoint)
        val ff = PageAndWaypoints(this, waypoints.setNextWaypoint(p.waypoint))
        println("\n>>>>CYAddddddd" + ff)
        ff
      case p: AddItemPage =>
        PageAndWaypoints(this, waypoints.setNextWaypoint(p.waypoint(CheckMode)))
    }
  }
}
