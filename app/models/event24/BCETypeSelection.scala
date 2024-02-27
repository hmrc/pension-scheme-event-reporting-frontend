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

package models.event24

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait BCETypeSelection

object BCETypeSelection extends Enumerable.Implicits {

  case object AnnuityProtection extends WithName("annuityProtection") with BCETypeSelection

  case object DefinedBenefit extends WithName("definedBenefit") with BCETypeSelection

  case object Drawdown extends WithName("drawdown") with BCETypeSelection

  case object FlexiAccess extends WithName("flexiAccess") with BCETypeSelection

  case object PensionProtection extends WithName("pensionProtection") with BCETypeSelection

  case object SeriousHealthLumpSum extends WithName("seriousHealthLumpSum") with BCETypeSelection

  case object Small extends WithName("small") with BCETypeSelection

  case object StandAlone extends WithName("standAlone") with BCETypeSelection

  case object TrivialCommutation extends WithName("trivialCommutation") with BCETypeSelection

  case object UncrystallisedFunds extends WithName("uncrystallisedFunds") with BCETypeSelection

  case object UncrystallisedFundsDeathBenefit extends WithName("uncrystallisedFundsDeathBenefit") with BCETypeSelection

  case object WindingUp extends WithName("windingUp") with BCETypeSelection


  val values: Seq[BCETypeSelection] = Seq(AnnuityProtection, DefinedBenefit, Drawdown, FlexiAccess, PensionProtection,
    SeriousHealthLumpSum, Small, StandAlone, TrivialCommutation, UncrystallisedFunds,
    UncrystallisedFundsDeathBenefit, WindingUp)

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"bceTypeSelection.event24.${value.toString.replaceAll("[, ]", "")}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[BCETypeSelection] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
