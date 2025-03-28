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

package viewmodels.event1.checkAnswers

import data.SampleData.employerAddress
import models.UserAnswers
import models.address.Address
import models.enumeration.AddressJourneyType
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.address.{IsUkPage, ManualAddressPage}
import pages.event1.Event1CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, SummaryListRow}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.address.checkAnswers.ChooseAddressSummary
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class ChooseAddressSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  private def addressAnswer(addr: Address)(implicit messages: Messages): Html = {
    def addrLineToHtml(l: String): String = s"""<span class="govuk-!-display-block">$l</span>"""

    def optionalAddrLineToHtml(optionalAddrLine: Option[String]): String = optionalAddrLine match {
      case Some(l) => addrLineToHtml(l)
      case None => ""
    }

    Html(
      addrLineToHtml(addr.addressLine1) +
        optionalAddrLineToHtml(addr.addressLine2) +
        addrLineToHtml(addr.townOrCity) +
        optionalAddrLineToHtml(addr.county) +
        optionalAddrLineToHtml(addr.postcode) +
        addrLineToHtml(messages("country." + addr.country))
    )
  }


  "row" - {

    "must display correct information" in {

      val addressJourneyType: AddressJourneyType = Event1EmployerAddressJourney
      val answer = UserAnswers().setOrException(ManualAddressPage(addressJourneyType, 0, true), employerAddress)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val isReadOnly = false

      val rowKey = if (addressJourneyType != Event1EmployerAddressJourney) {
        "residentialAddress.address.title"
      } else {
        "companyDetails.CYA.companyAddress"
      }

      val visuallyHiddenTextKey = if (addressJourneyType != Event1EmployerAddressJourney) {
        "enterPostcode.change.hidden"
      } else {
        "companyDetails.companyAddress.change.hidden"
      }

      val value = ValueViewModel(
        HtmlContent(
          addressAnswer(employerAddress)
        )
      )

      ChooseAddressSummary.row(answer, waypoints, 0, sourcePage, isReadOnly, addressJourneyType) mustBe Some(
        SummaryListRow(
          key = rowKey,
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", IsUkPage(addressJourneyType, 0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages(visuallyHiddenTextKey))
            )))
          }
        )
      )
    }
  }
}
