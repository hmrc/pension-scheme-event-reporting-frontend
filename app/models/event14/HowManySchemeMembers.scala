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

package models.event14

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import models.{Enumerable,WithName}

sealed trait HowManySchemeMembers

object HowManySchemeMembers extends Enumerable.Implicits {

  case object OptionOne extends WithName("0") with HowManySchemeMembers
  case object OptionTwo extends WithName("1") with HowManySchemeMembers
  case object OptionThree extends WithName("2 to 11") with HowManySchemeMembers
  case object OptionFour extends WithName("12 to 50") with HowManySchemeMembers
  case object OptionFive extends WithName("51 to 10,000") with HowManySchemeMembers
  case object OptionSix extends WithName("More than 10,000") with HowManySchemeMembers

  val values: Seq[HowManySchemeMembers] = Seq(
    OptionOne, OptionTwo, OptionThree, OptionFour, OptionFive, OptionSix
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"howManySchemeMembers.${value.toString.replaceAll("[, ]", "")}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[HowManySchemeMembers] =
    Enumerable(values.map(v => v.toString -> v)*)
}
