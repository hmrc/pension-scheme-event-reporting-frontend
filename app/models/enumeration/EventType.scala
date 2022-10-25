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

package models.enumeration

import play.api.mvc.{JavascriptLiteral, QueryStringBindable}

sealed trait EventType

object EventType extends Enumerable.Implicits {

  case object WindUp extends WithName("0") with EventType

  case object Event1 extends WithName("1") with EventType

  case object Event2 extends WithName("2") with EventType

  case object Event3 extends WithName("3") with EventType

  case object Event4 extends WithName("4") with EventType

  case object Event5 extends WithName("5") with EventType

  case object Event6 extends WithName("6") with EventType

  case object Event7 extends WithName("7") with EventType

  case object Event8 extends WithName("8") with EventType

  case object Event8A extends WithName("8A") with EventType

  case object Event10 extends WithName("10") with EventType

  case object Event11 extends WithName("11") with EventType

  case object Event12 extends WithName("12") with EventType

  case object Event13 extends WithName("13") with EventType

  case object Event14 extends WithName("14") with EventType

  case object Event18 extends WithName("18") with EventType

  case object Event19 extends WithName("19") with EventType

  case object Event20 extends WithName("20") with EventType

  case object Event20A extends WithName("20A") with EventType

  case object Event22 extends WithName("22") with EventType

  case object Event23 extends WithName("23") with EventType

  case object Event24 extends WithName("24") with EventType

  private val values: List[EventType] = List(WindUp, Event1, Event2, Event3, Event4, Event5, Event6, Event7, Event8, Event8A,
    Event10, Event11, Event12, Event13, Event14, Event18, Event19, Event20, Event20A, Event22, Event23, Event24)

  def getEventType(s: String): Option[EventType] = values.find(_.toString == s)

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[EventType] =
    new QueryStringBindable[EventType] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, EventType]] = {
        params.get(key).map {
          data =>
            EventType.getEventType(data.head)
              .map(Right(_))
              .getOrElse(Left(s"Unable to bind parameter ${data.head} as EventType"))
        }
      }

      override def unbind(key: String, value: EventType): String =
        stringBinder.unbind(key, value.toString)
    }
    
  implicit val jsLiteral: JavascriptLiteral[EventType] = (value: EventType) => value.toString
}
