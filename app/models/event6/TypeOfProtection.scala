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

package models.event6

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait TypeOfProtection

object TypeOfProtection extends Enumerable.Implicits {

  case object EnhancedLifetimeAllowance extends WithName("enhancedLifetimeAllowance") with TypeOfProtection

  case object EnhancedProtection extends WithName("enhancedProtection") with TypeOfProtection

  case object FixedProtection extends WithName("fixedProtection") with TypeOfProtection

  case object FixedProtection2014 extends WithName("fixedProtection2014") with TypeOfProtection

  case object FixedProtection2016 extends WithName("fixedProtection2016") with TypeOfProtection

  case object IndividualProtection2014 extends WithName("individualProtection2014") with TypeOfProtection

  case object IndividualProtection2016 extends WithName("individualProtection2016") with TypeOfProtection

  val values: Seq[TypeOfProtection] = Seq(
    EnhancedLifetimeAllowance, EnhancedProtection, FixedProtection, FixedProtection2014, FixedProtection2016,
    IndividualProtection2014, IndividualProtection2016
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"typeOfProtection.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[TypeOfProtection] =
    Enumerable(values.map(v => v.toString -> v)*)
}
