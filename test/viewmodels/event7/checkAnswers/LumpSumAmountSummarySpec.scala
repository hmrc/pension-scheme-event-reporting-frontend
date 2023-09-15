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

package viewmodels.event7.checkAnswers

import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event7.{Event7CheckYourAnswersPage, LumpSumAmountPage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, SummaryListRow}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class LumpSumAmountSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()


  "rowLumpSumAmount" - {

    "must display correct information for the amount" in {

      val lumpSumAmount = BigDecimal(1000000.00)
      val lumpSumAmountHtml = "£1,000,000.00"
      val isReadOnly = false
      val answer = UserAnswers().setOrException(LumpSumAmountPage(0), lumpSumAmount)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event7CheckYourAnswersPage(0)

      LumpSumAmountSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
        SummaryListRow(
          key = messages("lumpSumAmount.checkYourAnswersLabel"),
          value = ValueViewModel(HtmlContent(Html(lumpSumAmountHtml))),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", LumpSumAmountPage(index = 0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("lumpSumAmount.change.hidden"))
            )))
          }
        )
      )
    }
  }
}
