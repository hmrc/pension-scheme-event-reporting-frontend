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

package models.event22

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait HowAddAnnualAllowance

object HowAddAnnualAllowance extends Enumerable.Implicits {

  case object Manual extends WithName("manual") with HowAddAnnualAllowance

  case object FileUpload extends WithName("fileUpload") with HowAddAnnualAllowance

  val values: Seq[HowAddAnnualAllowance] = Seq(
    Manual, FileUpload
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"howAddAnnualAllowance.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index"),
        hint = if (value == FileUpload) Some(Hint(content = Text(messages("howAddAnnualAllowance.fileUpload.hint")))) else None,
        disabled = value == FileUpload
      )
  }

  implicit val enumerable: Enumerable[HowAddAnnualAllowance] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

