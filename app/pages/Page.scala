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

import models.{CheckMode, NormalMode, UserAnswers}
import play.api.mvc.Call
import queries.Gettable

final case class PageAndWaypoints(page: Page, waypoints: Waypoints) {

  lazy val route: Call = page.route(waypoints)
  lazy val url: String = route.url
}

trait Page {
  def navigate(waypoints: Waypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): PageAndWaypoints = {
    val targetPage            = nextPage(waypoints, originalAnswers, updatedAnswers)
    val recalibratedWaypoints = waypoints.recalibrate(this, targetPage)

    PageAndWaypoints(targetPage, recalibratedWaypoints)
  }

  private def nextPage(waypoints: Waypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page =
    waypoints match {
      case EmptyWaypoints =>
        nextPageNormalMode(waypoints, originalAnswers, updatedAnswers)

      case b: NonEmptyWaypoints =>
        b.currentMode match {
          case CheckMode  => nextPageCheckMode(b, originalAnswers, updatedAnswers)
          case NormalMode => nextPageNormalMode(b, originalAnswers, updatedAnswers)
        }
    }

  protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page =
    nextPageCheckMode(waypoints, updatedAnswers)

  protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    nextPageNormalMode(waypoints, answers, answers) match {
      case questionPage: Page with Gettable[_] =>
        if (answers.isDefined(questionPage)) waypoints.next.page else questionPage

      case otherPage =>
        otherPage
    }

  protected def nextPageNormalMode(waypoints: Waypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page =
    nextPageNormalMode(waypoints, updatedAnswers)

  protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    IndexPage

  def route(waypoints: Waypoints): Call

  def changeLink(waypoints: Waypoints, sourcePage: WaypointPage): PageAndWaypoints = {
    sourcePage match {
      case p: CheckAnswersPage =>
        PageAndWaypoints(this, waypoints.setNextWaypoint(p.waypoint))
      case p: AddItemPage =>
        PageAndWaypoints(this, waypoints.setNextWaypoint(p.waypoint(CheckMode)))
    }
  }
}
