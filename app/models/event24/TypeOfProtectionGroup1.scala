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
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait TypeOfProtectionGroup1

object TypeOfProtectionGroup1 extends Enumerable.Implicits {

  case object NonResidenceEnhancement extends WithName("nonResidenceEnhancement") with TypeOfProtectionGroup1

  case object PensionCreditsPreCRE extends WithName("pensionCreditsPreCRE") with TypeOfProtectionGroup1

  case object PreCommencement extends WithName("preCommencement") with TypeOfProtectionGroup1

  case object RecognisedOverseasPSTE extends WithName("recognisedOverseasPSTE") with TypeOfProtectionGroup1

  case object SchemeSpecific extends WithName("schemeSpecific") with TypeOfProtectionGroup1

  val values: Seq[TypeOfProtectionGroup1] = Seq(NonResidenceEnhancement,
    PensionCreditsPreCRE, PreCommencement, RecognisedOverseasPSTE, SchemeSpecific)

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] = values.zipWithIndex.map {
    case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"typeOfProtection.event24.${value.toString.replaceAll("[, ]", "")}")),
        fieldId = "value",
        value = value.toString,
        index = index
      )
  }

  implicit val enumerable: Enumerable[TypeOfProtectionGroup1] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
