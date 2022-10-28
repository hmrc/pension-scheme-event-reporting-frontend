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

import cats.data.NonEmptyList
import cats.implicits.toTraverseOps
import models.{Mode, NormalMode}
import play.api.mvc.QueryStringBindable

sealed trait Waypoints {

  val currentMode: Mode
  def setNextWaypoint(waypoint: Waypoint): Waypoints
  def recalibrate(currentPage: Page, targetPage: Page): Waypoints
}

case class NonEmptyWaypoints(waypoints: NonEmptyList[Waypoint]) extends Waypoints {

  val next: Waypoint = waypoints.head
  override val currentMode: Mode = next.mode

  override def setNextWaypoint(waypoint: Waypoint): Waypoints = {
    if (next == waypoint) {
      this
    } else {
      NonEmptyWaypoints(NonEmptyList(waypoint, waypoints.toList))
    }
  }

  override def recalibrate(currentPage: Page, targetPage: Page): Waypoints =
    (currentPage, targetPage) match {
      case (a: AddToListQuestionPage, b: AddToListQuestionPage) if a.section == b.section =>
        this

      case (_, targetPage: AddToListQuestionPage) =>
        setNextWaypoint(targetPage.addItemWaypoint)

      case _ =>
        if (next.page == targetPage) remove else this
    }

  override def toString: String = waypoints.toList.map(_.urlFragment).mkString(",")

  private def remove: Waypoints =
    waypoints.tail match {
      case Nil => EmptyWaypoints
      case t => NonEmptyWaypoints(NonEmptyList(t.head, t.tail))
    }
}

object EmptyWaypoints extends Waypoints {

  override val currentMode: Mode = NormalMode

  override def setNextWaypoint(waypoint: Waypoint): NonEmptyWaypoints =
    NonEmptyWaypoints(NonEmptyList(waypoint, Nil))

  override def recalibrate(currentPage: Page, targetPage: Page): Waypoints = this
}

object Waypoints {

  def apply(waypoints: List[Waypoint]): Waypoints =
    waypoints match {
      case Nil => EmptyWaypoints
      case h :: t => NonEmptyWaypoints(NonEmptyList(h, t))
    }

  def fromString(s: String): Option[Waypoints] =
    s.split(',').toList
      .map(Waypoint.fromString)
      .sequence
      .map(apply)

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[Waypoints] =
    new QueryStringBindable[Waypoints] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Waypoints]] = {
        params.get(key).map {
          data =>
            println( "\n>>>1>" + key)
            println( "\n>>>2>" + params)
            Waypoints.fromString(data.head)
              .map(Right(_))
              .getOrElse(Left(s"Unable to bind parameter ${data.head} as waypoints"))
        }
      }

      override def unbind(key: String, value: Waypoints): String =
        stringBinder.unbind(key, value.toString)
    }
}
