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

package models.event11

import play.api.libs.json.{Format, Json}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class Event11Date(date: LocalDate) {
  def formatEvent11Date: String = {
    date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
  }
}

object Event11Date {
  implicit val format: Format[Event11Date] = Json.format[Event11Date]
}

