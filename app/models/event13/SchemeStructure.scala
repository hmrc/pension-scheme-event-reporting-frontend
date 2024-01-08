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

package models.event13

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait SchemeStructure

object SchemeStructure extends Enumerable.Implicits {
  case object Single extends WithName("single") with SchemeStructure

  case object Group extends WithName("group") with SchemeStructure

  case object Corporate extends WithName("corporate") with SchemeStructure

  case object Other extends WithName("other") with SchemeStructure

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"schemeStructure.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  val values: Seq[SchemeStructure] = Seq(
    Single, Group, Corporate, Other
  )

  implicit val enumerable: Enumerable[SchemeStructure] =
    Enumerable(values.map(v => v.toString -> v): _*)
}