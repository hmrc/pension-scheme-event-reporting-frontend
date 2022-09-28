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

import models.UserAnswers
import models.requests.DataRequest
import pages.event1.employer.CompanyDetailsPage
import play.api.i18n.{Messages, MessagesProvider}
import play.api.mvc.{AnyContent, JavascriptLiteral, QueryStringBindable}
import viewmodels.Message
import viewmodels.Message.Literal

sealed trait AddressJourneyType {
  def eventType: EventType

  def nodeName: String

  def eventTypeFragment = s"event${eventType.toString}"

  def addressJourneyTypeFragment: String = nodeName

  val name: UserAnswers => Message
  val entityType: Message

  def heading(whichPage: String)(implicit
                                 request: DataRequest[AnyContent],
                                 provider: MessagesProvider): Message = {
    val specificHeadingMsgKey = s"${this.toString}.$whichPage.heading"
    val msgKeyHeading = if (Messages.isDefinedAt(specificHeadingMsgKey)) {
      specificHeadingMsgKey
    } else {
      s"$whichPage.heading"
    }
    Message(msgKeyHeading, this.name(request.userAnswers))
  }

  def title(whichPage: String)(implicit
                               request: DataRequest[AnyContent],
                               provider: MessagesProvider): Message = {
    val specificTitleMsgKey = s"${this.toString}.$whichPage.title"
    val msgKeyTitle = if (Messages.isDefinedAt(specificTitleMsgKey)) {
      specificTitleMsgKey
    } else {
      s"$whichPage.title"
    }
    Message(msgKeyTitle, entityType)
  }

}

abstract class WithJourneyTypeDetail(et: EventType, node: String) extends AddressJourneyType {
  override val toString: String = s"event${eventType.toString}.${nodeName}"

  override def eventType: EventType = et

  override def nodeName: String = node

  override val name: UserAnswers => Message
}

object AddressJourneyType extends Enumerable.Implicits {
  case object Event1EmployerAddressJourney extends WithJourneyTypeDetail(EventType.Event1, "employerAddress") with AddressJourneyType {
    override val entityType: Message = Message("entityType.theCompany")
    override val name: UserAnswers => Message = ua => ua.get(CompanyDetailsPage) match {
      case Some(cd) => Literal(cd.companyName)
      case _ => entityType
    }
  }

  // TODO: Remove this once we have at least two usages of the address journey. If only one instance then we get compile errors.
  case object DummyAddressJourney extends WithJourneyTypeDetail(EventType.Event1, "dummy") with AddressJourneyType {
    override val entityType: Message = Message("dummy entity type")
    override val name: UserAnswers => Message = _ => Message("dummy name")
  }

  private val values: List[AddressJourneyType] = List(Event1EmployerAddressJourney)

  def getEventType(s: String): Option[AddressJourneyType] = values.find(_.toString == s)

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[AddressJourneyType] =
    new QueryStringBindable[AddressJourneyType] {

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, AddressJourneyType]] = {
        params.get(key).map {
          data =>
            AddressJourneyType.getEventType(data.head)
              .map(Right(_))
              .getOrElse(Left(s"Unable to bind parameter ${data.head} as AddressJourneyType"))
        }
      }

      override def unbind(key: String, value: AddressJourneyType): String =
        stringBinder.unbind(key, value.toString)
    }

  implicit val jsLiteral: JavascriptLiteral[AddressJourneyType] = new JavascriptLiteral[AddressJourneyType] {
    override def to(value: AddressJourneyType): String = value match {
      case Event1EmployerAddressJourney => Event1EmployerAddressJourney.toString
      case DummyAddressJourney => DummyAddressJourney.toString
    }
  }
}

