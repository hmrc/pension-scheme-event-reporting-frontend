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

package viewmodels.event6.checkAnswers

import models.UserAnswers
import models.enumeration.EventType.Event6
import models.event6.CrystallisedDetails
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event6.{AmountCrystallisedAndDatePage, Event6CheckYourAnswersPage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, SummaryListRow}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class AmountCrystallisedAndDateSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()
  private val eventType = Event6


  "rowCrystallisedDetails" - {

    "must display correct information for the amount and date crystallised" in {

      val crystallisedDetails = CrystallisedDetails(1000000.00, LocalDate.now())
      val amountCrystallised = "£1,000,000.00"
      val isReadOnly = false
      val answer = UserAnswers().setOrException(AmountCrystallisedAndDatePage(eventType, 0), crystallisedDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event6CheckYourAnswersPage(0)
      val format = DateTimeFormatter.ofPattern("dd MMMM yyyy")

      val htmlContent = HtmlContent(
        s"""<p class="govuk-body">${amountCrystallised}</p>
           |<p class="govuk-body">${format.format(crystallisedDetails.crystallisedDate)}</p>""".stripMargin)

      AmountCrystallisedAndDateSummary.rowCrystallisedDetails(answer, waypoints, sourcePage, isReadOnly, 0) mustBe Some(
        SummaryListRow(
          key = messages("amountCrystallisedAndDate.value.checkYourAnswersLabel"),
          value = ValueViewModel(htmlContent),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", AmountCrystallisedAndDatePage(eventType, index = 0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("amountCrystallisedAndDate.value.change.hidden"))
            )))
          }
        )
      )
    }
  }
}
