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

sealed trait VersionStatus

object VersionStatus extends Enumerable.Implicits {
  case object NotStarted extends WithName("notStarted") with VersionStatus

  case object Compiled extends WithName("compiled") with VersionStatus

  case object Submitted extends WithName("submitted") with VersionStatus

  private val values: List[VersionStatus] = List(NotStarted, Compiled, Submitted)

  implicit val enumerable: Enumerable[VersionStatus] =
    Enumerable(values.map(v => v.toString -> v)*)
}
