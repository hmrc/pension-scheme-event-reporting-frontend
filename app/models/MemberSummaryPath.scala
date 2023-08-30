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

package models

import models.enumeration.EventType
import play.api.mvc.PathBindable

case class MemberSummaryPath(event:EventType)

object MemberSummaryPath {
  implicit def pathBinder: PathBindable[MemberSummaryPath] = new PathBindable[MemberSummaryPath] {
    override def bind(key: String, value: String): Either[String, MemberSummaryPath] = {
      val splitString = value.split('-')
      if(splitString.length == 3) {
        val eventName = splitString(1)
        EventType.mapOfEvents.get(eventName)
          .map(x => Right(MemberSummaryPath(x))).getOrElse(Left("Unknown event type"))
      } else {
        Left("Unknown url format")
      }
    }

    override def unbind(key: String, eventSummaryPath: MemberSummaryPath): String = {
      "event-" + eventSummaryPath.event.toString + "-summary"
    }
  }
}
