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

import models.UserAnswers
import models.common.ManualOrUpload
import models.enumeration.EventType.Event1
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.common.ManualOrUploadPage
import pages.event1.Event1CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.ManualOrUploadSummary
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class ManualOrUploadSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()
  private val event = Event1

  "row" - {

    "must display correct information for who received the unauthorised payment or deemed unauthorised payment (Manual)" in {

      val answer = UserAnswers().setOrException(ManualOrUploadPage(event, 0), ManualOrUpload.Manual)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"manualOrUpload.event1.${ManualOrUpload.Manual}"))
        )
      )

      ManualOrUploadSummary.row(answer, waypoints, event, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "manualOrUpload.event1.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", ManualOrUploadPage(event, 0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("manualOrUpload.event1.change.hidden"))
          )
        )
      )
    }

    "must display correct information for who received the unauthorised payment or deemed unauthorised payment (FileUpload)" in {

      val answer = UserAnswers().setOrException(ManualOrUploadPage(event, 0), ManualOrUpload.FileUpload)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"manualOrUpload.event1.${ManualOrUpload.FileUpload}"))
        )
      )

      ManualOrUploadSummary.row(answer, waypoints, event, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "manualOrUpload.event1.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", ManualOrUploadPage(event, 0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("manualOrUpload.event1.change.hidden"))
          )
        )
      )
    }
  }
}
