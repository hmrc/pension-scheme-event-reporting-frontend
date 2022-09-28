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
import play.api.mvc.{AnyContent, JavascriptLiteral, QueryStringBindable}
import viewmodels.Message
import viewmodels.Message.Literal

sealed trait AddressJourneyType {
  val eventType: EventType
  val nodeName: String
  def entityTypeInstanceName(ua:UserAnswers):Message

  def heading(whichPage: String)(implicit request: DataRequest[AnyContent]): Message

  def title(whichPage: String): Message
}

abstract class WithJourneyTypeDetail(val eventType: EventType, val nodeName: String, entityTypeMessageKey: String) extends AddressJourneyType {
  override def toString: String = s"event${this.eventType.toString}.$nodeName"

  override def heading(whichPage: String)(implicit
                                          request: DataRequest[AnyContent]): Message =
    Message(s"$whichPage.heading", this.entityTypeInstanceName(request.userAnswers))

  override def title(whichPage: String): Message = Message(s"$whichPage.title",
    Message(entityTypeMessageKey))
}

object AddressJourneyType extends Enumerable.Implicits {
  private val entityTypeMessageKeyCompany = "entityType.theCompany"
  case object Event1EmployerAddressJourney extends WithJourneyTypeDetail(
    eventType = EventType.Event1,
    nodeName = "employerAddress",
    entityTypeMessageKey = entityTypeMessageKeyCompany) {
    override def entityTypeInstanceName(ua: UserAnswers): Message = ua.get(CompanyDetailsPage) match {
      case Some(cd) => Literal(cd.companyName)
      case _ => Message(entityTypeMessageKeyCompany)
    }
  }

  // TODO: Remove this dummy object when we have at least two usages of the AddressJourneyType. If only one instance then we get compile errors.
  case object DummyAddressJourney extends WithJourneyTypeDetail(
    eventType = EventType.Event1,
    nodeName = "dummyNodeName",
    entityTypeMessageKey = "entityTypeMessageKey") {
    override def entityTypeInstanceName(ua: UserAnswers): Message = Message("dummy name")

    // Examples below as to how to override the header/ title message key on address pages if necessary:-
    //
    //    override def heading(whichPage: String)(implicit
    //                                            request: DataRequest[AnyContent]): Message = {
    //      whichPage match {
    //        case "chooseAddress" => Message("another-message-keya", this.name(request.userAnswers))
    //        case _ => super.heading(whichPage)
    //      }
    //    }
    //
    //    override def title(whichPage: String): Message = {
    //      whichPage match {
    //        case "chooseAddress" => Message("another-message-keyb", this.name(request.userAnswers))
    //        case _ => super.heading(whichPage)
    //      }
    //    }
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

  implicit val jsLiteral: JavascriptLiteral[AddressJourneyType] = (value: AddressJourneyType) => value.toString
}

