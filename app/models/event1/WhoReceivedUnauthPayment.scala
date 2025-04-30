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

package models.event1

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait WhoReceivedUnauthPayment

object WhoReceivedUnauthPayment extends Enumerable.Implicits {

  case object Member extends WithName("member") with WhoReceivedUnauthPayment
  case object Employer extends WithName("employer") with WhoReceivedUnauthPayment

  val values: Seq[WhoReceivedUnauthPayment] = Seq(
    Member, Employer
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"whoReceivedUnauthPayment.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index"),
        hint    = if (value == Member) Some(Hint(content = Text(messages("whoReceivedUnauthPayment.member.hint")))) else None
      )
  }

  implicit val enumerable: Enumerable[WhoReceivedUnauthPayment] =
    Enumerable(values.map(v => v.toString -> v)*)
}
