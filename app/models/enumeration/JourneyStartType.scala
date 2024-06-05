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

package models.enumeration

sealed trait JourneyStartType

object JourneyStartType extends Enumerable.Implicits {

  case object StartNew extends WithName("startNew") with JourneyStartType

  case object InProgress extends WithName("inProgress") with JourneyStartType

  case object PastEventTypes extends WithName("pastEventTypes") with JourneyStartType

  private val values: List[JourneyStartType] = List(StartNew, InProgress, PastEventTypes)
  implicit val enumerable: Enumerable[JourneyStartType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
