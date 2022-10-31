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
import pages.Page
import pages.address.{ChooseAddressPage, EnterPostcodePage, ManualAddressPage}
import pages.event1.employer.CompanyDetailsPage
import play.api.mvc.{AnyContent, JavascriptLiteral, QueryStringBindable}
import viewmodels.Message
import viewmodels.Message.Literal

sealed trait AddressJourneyType {
  val eventType: EventType
  val nodeName: String
  def entityName(ua:UserAnswers, index: Int):Message

  def heading(whichPage: Page, index: Int)(implicit request: DataRequest[AnyContent]): Message

  def title(whichPage: Page): Message
}

abstract class WithJourneyTypeDetail(val eventType: EventType, val nodeName: String, entityTypeMessageKey: String) extends AddressJourneyType {
  override def toString: String = s"event${this.eventType.toString}.$nodeName"

  override def heading(whichPage: Page, index: Int)(implicit
                                          request: DataRequest[AnyContent]): Message =
    Message(s"${whichPage.toString}.heading", this.entityName(request.userAnswers, index))

  override def title(whichPage: Page): Message = Message(s"${whichPage.toString}.title",
    Message(entityTypeMessageKey))
}

object AddressJourneyType extends Enumerable.Implicits {
  private val entityTypeMessageKeyCompany = "entityType.theCompany"
  private val entityTypeMessageKeyResidentialProperty = "entityType.theResidentialProperty"

  case object Event1EmployerAddressJourney extends WithJourneyTypeDetail(
    eventType = EventType.Event1,
    nodeName = "employerAddress",
    entityTypeMessageKey = entityTypeMessageKeyCompany) {
    override def entityName(ua: UserAnswers, index: Int): Message = ua.get(CompanyDetailsPage(index)) match {
      case Some(cd) => Literal(cd.companyName)
      case _ => Message(entityTypeMessageKeyCompany)
    }
  }

  case object Event1MemberPropertyAddressJourney extends WithJourneyTypeDetail(
    eventType = EventType.Event1,
    nodeName = "memberResidentialAddress",
    entityTypeMessageKey = entityTypeMessageKeyResidentialProperty) {
    override def entityName(ua: UserAnswers, index: Int): Message = Message(entityTypeMessageKeyResidentialProperty)
    override def heading(whichPage: Page, index: Int)(implicit
                                          request: DataRequest[AnyContent]): Message =
      whichPage match {
        case EnterPostcodePage(_, _) => Message("residentialAddress.enterPostcode.h1")
        case ChooseAddressPage(_, _) => Message("residentialAddress.chooseAddress.h1")
        case ManualAddressPage(_, _) => Message("residentialAddress.address.h1")
      }

    override def title(whichPage: Page): Message =
      whichPage match {
        case EnterPostcodePage(_, _) => Message("residentialAddress.enterPostcode.title")
        case ChooseAddressPage(_, _) => Message("residentialAddress.chooseAddress.title")
        case ManualAddressPage(_, _) => Message("residentialAddress.address.title")
      }
  }

  case object Event1EmployerPropertyAddressJourney extends WithJourneyTypeDetail(
    eventType = EventType.Event1,
    nodeName = "employerResidentialAddress",
    entityTypeMessageKey = entityTypeMessageKeyResidentialProperty) {
    override def entityName(ua: UserAnswers, index: Int): Message = Message(entityTypeMessageKeyResidentialProperty)

    override def heading(whichPage: Page, index: Int)(implicit
                                          request: DataRequest[AnyContent]): Message =
      whichPage match {
        case EnterPostcodePage(_, _) => Message("residentialAddress.enterPostcode.h1")
        case ChooseAddressPage(_, _) => Message("residentialAddress.chooseAddress.h1")
        case ManualAddressPage(_, _) => Message("residentialAddress.address.h1")
      }

    override def title(whichPage: Page): Message =
      whichPage match {
        case EnterPostcodePage(_, _) => Message("residentialAddress.enterPostcode.title")
        case ChooseAddressPage(_, _) => Message("residentialAddress.chooseAddress.title")
        case ManualAddressPage(_, _) => Message("residentialAddress.address.title")
      }
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

