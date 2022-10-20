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

package viewmodels.event1.checkAnswers

import base.SpecBase
import data.SampleData.employerAddress
import models.UserAnswers
import models.address.Address
import models.enumeration.AddressJourneyType
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import models.enumeration.EventType.Event1
import pages.address.{EnterPostcodePage, ManualAddressPage}
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.address.checkAnswers.ChooseAddressSummary
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class ChooseAddressSummarySpec extends SpecBase with SummaryListFluency {

  private def addressAnswer(addr: Address)(implicit messages: Messages): Html = {
    def addrLineToHtml(l: String): String = s"""<span class="govuk-!-display-block">$l</span>"""

    def optionalAddrLineToHtml(optionalAddrLine: Option[String]): String = optionalAddrLine match {
      case Some(l) => addrLineToHtml(l)
      case None => ""
    }

    Html(
      addrLineToHtml(addr.addressLine1) +
        addrLineToHtml(addr.addressLine2) +
        optionalAddrLineToHtml(addr.addressLine3) +
        optionalAddrLineToHtml(addr.addressLine4) +
        optionalAddrLineToHtml(addr.postcode) +
        addrLineToHtml(messages("country." + addr.country))
    )
  }


  "row" - {

    "must display correct information" in {

      val addressJourneyType: AddressJourneyType = Event1EmployerAddressJourney
      val answer = UserAnswers().setOrException(ManualAddressPage(addressJourneyType), employerAddress)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)


      val value = ValueViewModel(
        HtmlContent(
          addressAnswer(employerAddress)
        )
      )

      ChooseAddressSummary.row(answer, waypoints, sourcePage, addressJourneyType) mustBe Some(
        SummaryListRowViewModel(
          key = "companyDetails.CYA.companyAddress",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", EnterPostcodePage(addressJourneyType).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("enterPostcode.change.hidden"))
          )
        )
      )
    }
  }
}
