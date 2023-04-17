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
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class AmountCrystallisedAndDateSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()
  private val eventType = Event6


  "rowAmountCrystallised" - {

    "must display correct information for the amount crystallised" in {

      val crystallisedDetails = CrystallisedDetails(1000000.00, LocalDate.now())
      val amountCrystallised = "£1,000,000.00"

      val answer = UserAnswers().setOrException(AmountCrystallisedAndDatePage(eventType, 0), crystallisedDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event6CheckYourAnswersPage(0)

      AmountCrystallisedAndDateSummary.rowCrystallisedValue(answer, waypoints, sourcePage, eventType, 0) mustBe Some(
        SummaryListRowViewModel(
          key = messages("amountCrystallisedAndDate.value.checkYourAnswersLabel"),
          value = ValueViewModel(HtmlContent(Html(amountCrystallised))),
          actions = Seq(
            ActionItemViewModel("site.change", AmountCrystallisedAndDatePage(eventType, index = 0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("amountCrystallisedAndDate.value.change.hidden"))
          )
        )
      )
    }
  }

  "rowCrystallisedDate" - {

    "must display correct information for the crystallised date" in {

      val crystallisedDetails = CrystallisedDetails(1000.00, LocalDate.now())

      val answer = UserAnswers().setOrException(AmountCrystallisedAndDatePage(eventType, 0), crystallisedDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event6CheckYourAnswersPage(0)

      val date = crystallisedDetails.crystallisedDate
      val format = DateTimeFormatter.ofPattern("dd MMMM yyyy")

      AmountCrystallisedAndDateSummary.rowCrystallisedDate(answer, waypoints, sourcePage, eventType, 0) mustBe Some(
        SummaryListRowViewModel(
          key = messages("amountCrystallisedAndDate.date.checkYourAnswersLabel"),
          value = ValueViewModel(format.format(date)),
          actions = Seq(
            ActionItemViewModel("site.change", AmountCrystallisedAndDatePage(eventType, 0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("amountCrystallisedAndDate.date.change.hidden"))
          )
        )
      )
    }
  }
}
