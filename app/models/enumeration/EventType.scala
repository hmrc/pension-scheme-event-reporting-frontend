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

package models.enumeration

import models.EventSelection
import play.api.mvc.{JavascriptLiteral, QueryStringBindable}

sealed trait EventType

object EventType extends Enumerable.Implicits {

  def fromEventSelection(es: EventSelection): Option[EventType] = {
    es match {
      case EventSelection.Event1 => Some(EventType.Event1)
      case EventSelection.Event2 => Some(EventType.Event2)
      case EventSelection.Event3 => Some(EventType.Event3)
      case EventSelection.Event4 => Some(EventType.Event4)
      case EventSelection.Event5 => Some(EventType.Event5)
      case EventSelection.Event6 => Some(EventType.Event6)
      case EventSelection.Event7 => Some(EventType.Event7)
      case EventSelection.Event8 => Some(EventType.Event8)
      case EventSelection.Event8A => Some(EventType.Event8A)
      case EventSelection.Event10 => Some(EventType.Event10)
      case EventSelection.Event11 => Some(EventType.Event11)
      case EventSelection.Event12 => Some(EventType.Event12)
      case EventSelection.Event13 => Some(EventType.Event13)
      case EventSelection.Event14 => Some(EventType.Event14)
      case EventSelection.Event18 => Some(EventType.Event18)
      case EventSelection.Event19 => Some(EventType.Event19)
      case EventSelection.Event20 => Some(EventType.Event20)
      case EventSelection.Event20A => Some(EventType.Event20A)
      case EventSelection.Event22 => Some(EventType.Event22)
      case EventSelection.Event23 => Some(EventType.Event23)
      case EventSelection.EventWoundUp => Some(EventType.WindUp)
      case _ => None
    }
  }

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

  def getEventTypeByName(eventType: EventType): String = {
    eventType match {
      case Event22 => "annual allowance"
      case Event23 => "dual annual allowances"
      case _ => "EventTypeByName needs to be implemented for other events"
    }
  }

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

  def toRoute(eventType: EventType): String = eventType match {
    case Event22 => "event-22"
    case Event23 => "event-23"
    case _ => throw new RuntimeException(s"Unimplemented event type: $eventType")
  }

  implicit val jsLiteral: JavascriptLiteral[EventType] = (value: EventType) => value.toString

  implicit val enumerable: Enumerable[EventType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
