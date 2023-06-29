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

package models.common

import models.enumeration.EventType
import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ManualOrUpload

object ManualOrUpload extends Enumerable.Implicits {

  case object Manual extends WithName("manual") with ManualOrUpload

  case object FileUpload extends WithName("fileUpload") with ManualOrUpload

  def values: Seq[ManualOrUpload] = {
    Seq(Manual, FileUpload)
  }

  def options(eventType: EventType)(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"manualOrUpload.event${eventType.toString}.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index"),
        hint = if (value == FileUpload) Some(Hint(content = Text(messages(s"manualOrUpload.event${eventType.toString}.fileUpload.hint")))) else None,
        disabled = (value, eventType) match {
          case (FileUpload, EventType.Event22) => false
          case (Manual, EventType.Event6) => false
          case (Manual, EventType.Event1) => false
          case (_, EventType.Event22) => false
          case (_, EventType.Event23) => false
          case _ => true
        }
      )
  }

  implicit val enumerable: Enumerable[ManualOrUpload] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
