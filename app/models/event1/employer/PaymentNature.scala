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

package models.event1.employer

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import models.{Enumerable,WithName}

sealed trait PaymentNature

object PaymentNature extends Enumerable.Implicits {

  case object LoansExceeding50PercentOfFundValue extends WithName("loansExceeding50PercentOfFundValue") with PaymentNature
  case object ResidentialProperty extends WithName("residentialProperty") with PaymentNature
  case object TangibleMoveableProperty extends WithName("tangibleMoveableProperty") with PaymentNature
  case object CourtOrder extends WithName("courtOrder") with PaymentNature
  case object Other extends WithName("other") with PaymentNature

  val values: Seq[PaymentNature] = Seq(
    LoansExceeding50PercentOfFundValue, ResidentialProperty, TangibleMoveableProperty, CourtOrder, Other
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"paymentNature.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[PaymentNature] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
