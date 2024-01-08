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

package viewmodels.address.checkAnswers

import models.address.Address
import models.enumeration.AddressJourneyType
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import models.{Index, UserAnswers}
import org.apache.commons.lang3.StringUtils
import pages.address.{EnterPostcodePage, ManualAddressPage}
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ChooseAddressSummary {

  private def addressAnswer(addr: Address)(implicit messages: Messages): Html = {
    def addrLineToHtml(l: String): String = s"""<span class="govuk-!-display-block">$l</span>"""

    def optionalAddrLineToHtml(optionalAddrLine: Option[String]): String = optionalAddrLine match {
      case Some(l) => addrLineToHtml(l)
      case None => StringUtils.EMPTY
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

  def row(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage, isReadOnly: Boolean, addressJourneyType: AddressJourneyType)
         (implicit messages: Messages): Option[SummaryListRow] = {

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

    answers.get(ManualAddressPage(addressJourneyType, index)).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            addressAnswer(answer)
          )
        )

        SummaryListRow(
          key = rowKey,
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", EnterPostcodePage(addressJourneyType, index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages(visuallyHiddenTextKey))
            )))
          }
        )
    }
  }
}
