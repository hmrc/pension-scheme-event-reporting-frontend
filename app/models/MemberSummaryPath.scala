/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import models.enumeration.EventType
import play.api.mvc.PathBindable

case class MemberSummaryPath(event:EventType)

object MemberSummaryPath {

  private val restrictedEventTypes = Set(
    "10", "11", "12", "13", "14", "18", "19", "20", "20A"
  )

  implicit def pathBinder: PathBindable[MemberSummaryPath] = new PathBindable[MemberSummaryPath] {
    override def bind(key: String, value: String): Either[String, MemberSummaryPath] = {
      val splitString = value.split('-')
      if (splitString.length == 3 && splitString(0) == "event" && splitString(2) == "summary") {
        val eventName = splitString(1)
        if (restrictedEventTypes.contains(eventName)) {
          Left(s"Event type '$eventName' does not have a relevant summary page.")
        } else {
          EventType.mapOfEvents.get(eventName)
            .map(eventType => Right(MemberSummaryPath(eventType)))
            .getOrElse(Left(s"Unknown event type '$eventName'"))
        }
      } else {
        Left("Unknown URL format")
      }
    }


    override def unbind(key: String, eventSummaryPath: MemberSummaryPath): String = {
      s"event-${eventSummaryPath.event.toString}-summary"
    }
  }
}
