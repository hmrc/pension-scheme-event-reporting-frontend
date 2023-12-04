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

package models.event24

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait TypeOfProtectionSelection

object TypeOfProtectionSelection extends Enumerable.Implicits {
  case object EnhancedProtection extends WithName("enhancedProtection") with TypeOfProtectionSelection

  case object EnhancedProtectionWithProtectedSum extends WithName("enhancedProtectionWithProtectedSum") with TypeOfProtectionSelection

  case object FixedProtection extends WithName("fixedProtection") with TypeOfProtectionSelection

  case object FixedProtection2014 extends WithName("fixedProtection2014") with TypeOfProtectionSelection

  case object FixedProtection2016 extends WithName("fixedProtection2016") with TypeOfProtectionSelection

  case object IndividualProtection2014 extends WithName("individualProtection2014") with TypeOfProtectionSelection

  case object IndividualProtection2016 extends WithName("individualProtection2016") with TypeOfProtectionSelection

  case object NonResidenceEnhancement extends WithName("nonResidenceEnhancement") with TypeOfProtectionSelection

  case object PensionCreditsPreCRE extends WithName("pensionCreditsPreCRE") with TypeOfProtectionSelection

  case object PreCommencement extends WithName("preCommencement") with TypeOfProtectionSelection

  case object Primary extends WithName("primary") with TypeOfProtectionSelection

  case object PrimaryWithProtectedSum extends WithName("primaryWithProtectedSum") with TypeOfProtectionSelection

  case object RecognisedOverseasPSTE extends WithName("recognisedOverseasPSTE") with TypeOfProtectionSelection

  case object SchemeSpecific extends WithName("schemeSpecific") with TypeOfProtectionSelection

  val values: Seq[TypeOfProtectionSelection] = Seq(EnhancedProtection, EnhancedProtectionWithProtectedSum, FixedProtection,
    FixedProtection2014, FixedProtection2016, IndividualProtection2014, IndividualProtection2016, NonResidenceEnhancement,
    PensionCreditsPreCRE, PreCommencement, Primary, PrimaryWithProtectedSum, RecognisedOverseasPSTE, SchemeSpecific)

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"typeOfProtection.event24.${value.toString.replaceAll("[, ]", "")}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[TypeOfProtectionSelection] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
