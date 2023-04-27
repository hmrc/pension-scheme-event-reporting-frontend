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
import pages.event7.{CrystallisedAmountPage, Event7CheckYourAnswersPage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class CrystallisedAmountSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()


  "rowCrystallisedAmount" - {

    "must display correct information for the amount" in {

      val crystallisedAmount = BigDecimal(1000000.00)
      val crystallisedAmountHtml = "£1,000,000.00"

      val answer = UserAnswers().setOrException(CrystallisedAmountPage(0), crystallisedAmount)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event7CheckYourAnswersPage(0)

      CrystallisedAmountSummary.row(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = messages("crystallisedAmount.checkYourAnswersLabel"),
          value = ValueViewModel(HtmlContent(Html(crystallisedAmountHtml))),
          actions = Seq(
            ActionItemViewModel("site.change", CrystallisedAmountPage(index = 0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("crystallisedAmount.change.hidden"))
          )
        )
      )
    }
  }
}
