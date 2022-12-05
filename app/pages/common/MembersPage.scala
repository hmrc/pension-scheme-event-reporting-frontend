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

package pages.common

import models.Index
import models.common.MembersSummary
import models.enumeration.EventType
import play.api.libs.json.JsPath
import queries.Gettable

case class MembersPage(eventType: EventType) extends Gettable[Seq[MembersSummary]] {
  def apply(index: Index): JsPath = path \ index

  def path: JsPath = JsPath \ s"event${eventType.toString}" \ toString

  override def toString: String = "members"

}
