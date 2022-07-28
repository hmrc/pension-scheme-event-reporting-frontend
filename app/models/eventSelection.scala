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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait eventSelection

object eventSelection extends Enumerable.Implicits {

  case object Event1 extends WithName("event1") with eventSelection
  case object Event2 extends WithName("event2") with eventSelection
  case object Event3 extends WithName("event3") with eventSelection
  case object Event4 extends WithName("event4") with eventSelection
  case object Event5 extends WithName("event5") with eventSelection
  case object Event6 extends WithName("event6") with eventSelection
  case object Event7 extends WithName("event7") with eventSelection
  case object Event8 extends WithName("event8") with eventSelection
  case object Event8a extends WithName("event8a") with eventSelection
  case object Event10 extends WithName("event10") with eventSelection
  case object Event11 extends WithName("event11") with eventSelection
  case object Event12 extends WithName("event12") with eventSelection
  case object Event13 extends WithName("event13") with eventSelection
  case object Event14 extends WithName("event14") with eventSelection
  case object Event18 extends WithName("event18") with eventSelection
  case object Event19 extends WithName("event19") with eventSelection
  case object Event20 extends WithName("event20") with eventSelection
  case object Event20a extends WithName("event20a") with eventSelection
  case object Event22 extends WithName("event22") with eventSelection
  case object Event23 extends WithName("event23") with eventSelection
  case object Event24 extends WithName("event24") with eventSelection
  case object Or extends WithName("or") with eventSelection
  case object EventWoundUp extends WithName("eventWoundUp") with eventSelection

  val values: Seq[eventSelection] = Seq(
    Event1, Event2, Event3, Event4, Event5, Event6, Event7, Event8, Event8a, Event10,
    Event11, Event12, Event13, Event14, Event18, Event19, Event20, Event20a, Event22,
    Event23, Event24, Or, EventWoundUp
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      if( value.toString == "or" ) {
        RadioItem(
          divider = Some("or")
        )
      } else {
        RadioItem(
          content = Text(messages(s"eventSelection.${value.toString}")),
          value   = Some(value.toString),
          id      = Some(s"value_$index"),
          hint    = if(value.toString != "eventWoundUp"){
            Some(Hint(
              content = Text(messages(s"eventSelection.${value.toString}.hint"))
            ))
          } else { None }
        )
      }
  }

  implicit val enumerable: Enumerable[eventSelection] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
