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

package viewmodels.event8.checkAnswers

import data.SampleData.lumpSumDetails
import models.UserAnswers
import models.enumeration.EventType.Event8
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event8.{Event8CheckYourAnswersPage, LumpSumAmountAndDatePage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import viewmodels.event8.checkAnswers.LumpSumAmountAndDateSummary.{currencyFormatter, dateFormatter}
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

class LumpSumAmountAndDateSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {
  private implicit val messages: Messages = stubMessages()
  "rowLumpSumValue" - {
    "must display correct information for the sum amount" in {
      val answer = UserAnswers().setOrException(LumpSumAmountAndDatePage(Event8, 0), lumpSumDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event8CheckYourAnswersPage(0)

      LumpSumAmountAndDateSummary.rowLumpSumValue(answer, waypoints, sourcePage, Event8 ,0) mustBe Some(
        SummaryListRowViewModel(
          key = messages("lumpSumAmountAndDate.value.checkYourAnswersLabel"),
          value = ValueViewModel(HtmlContent(s"£${currencyFormatter.format(lumpSumDetails.lumpSumAmount)}")),
          actions = Seq(
            ActionItemViewModel("site.change", LumpSumAmountAndDatePage(Event8, index = 0 ).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("lumpSumAmountAndDate.value.change.hidden"))
          )
        )
      )
    }
  }

  "rowLumpSumDate" - {
    "must display correct information for the sum date" in {
      val answer = UserAnswers().setOrException(LumpSumAmountAndDatePage(Event8, 0), lumpSumDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event8CheckYourAnswersPage(0)

      LumpSumAmountAndDateSummary.rowLumpSumDate(answer, waypoints, sourcePage, Event8, 0) mustBe Some(
        SummaryListRowViewModel(
          key = messages("lumpSumAmountAndDate.date.checkYourAnswersLabel"),
          value = ValueViewModel(dateFormatter.format(lumpSumDetails.lumpSumDate)),
          actions = Seq(
            ActionItemViewModel("site.change", LumpSumAmountAndDatePage(Event8, index = 0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("lumpSumAmountAndDate.date.change.hidden"))
          )
        )
      )
    }
  }

}
