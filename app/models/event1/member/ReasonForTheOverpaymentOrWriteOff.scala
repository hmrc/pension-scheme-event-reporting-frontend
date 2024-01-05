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

package models.event1.member

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ReasonForTheOverpaymentOrWriteOff

object ReasonForTheOverpaymentOrWriteOff extends Enumerable.Implicits {

  case object DeathOfMember extends WithName("deathOfMember") with ReasonForTheOverpaymentOrWriteOff

  case object DeathOfDependent extends WithName("deathOfDependent") with ReasonForTheOverpaymentOrWriteOff

  case object DependentNoLongerQualifiedForPension extends WithName("dependentNoLongerQualifiedForPension") with ReasonForTheOverpaymentOrWriteOff

  case object Other extends WithName("other") with ReasonForTheOverpaymentOrWriteOff

  val values: Seq[ReasonForTheOverpaymentOrWriteOff] = Seq(
    DeathOfMember, DeathOfDependent, DependentNoLongerQualifiedForPension, Other
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"reasonForTheOverpaymentOrWriteOff.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[ReasonForTheOverpaymentOrWriteOff] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
