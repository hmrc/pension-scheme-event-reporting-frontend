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
import models.event1.member.ReasonForTheOverpaymentOrWriteOff
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.Event1CheckYourAnswersPage
import pages.event1.member.ReasonForTheOverpaymentOrWriteOffPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, SummaryListRow}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class ReasonForTheOverpaymentOrWriteOffSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  "row" - {

    "must display correct information for reason for the overpayment/write off (Death of member)" in {

      val answer = UserAnswers().setOrException(ReasonForTheOverpaymentOrWriteOffPage(0), ReasonForTheOverpaymentOrWriteOff.DeathOfMember)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val isReadOnly = false
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"reasonForTheOverpaymentOrWriteOff.${ReasonForTheOverpaymentOrWriteOff.DeathOfMember}"))
        )
      )

      ReasonForTheOverpaymentOrWriteOffSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
        SummaryListRow(
          key = "reasonForTheOverpaymentOrWriteOff.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", ReasonForTheOverpaymentOrWriteOffPage(0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("reasonForTheOverpaymentOrWriteOff.change.hidden"))
            )))
          }
        )
      )
    }

    "must display correct information for reason for the overpayment/write off (Death of dependent)" in {

      val answer = UserAnswers().setOrException(ReasonForTheOverpaymentOrWriteOffPage(0), ReasonForTheOverpaymentOrWriteOff.DeathOfDependent)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val isReadOnly = false
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"reasonForTheOverpaymentOrWriteOff.${ReasonForTheOverpaymentOrWriteOff.DeathOfDependent}"))
        )
      )

      ReasonForTheOverpaymentOrWriteOffSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
        SummaryListRow(
          key = "reasonForTheOverpaymentOrWriteOff.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", ReasonForTheOverpaymentOrWriteOffPage(0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("reasonForTheOverpaymentOrWriteOff.change.hidden"))
            )))
          }
        )
      )
    }

    "must display correct information for reason for the overpayment/write off (Dependent No Longer Qualified For Pension)" in {

      val answer = UserAnswers().setOrException(ReasonForTheOverpaymentOrWriteOffPage(0), ReasonForTheOverpaymentOrWriteOff.DependentNoLongerQualifiedForPension)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val isReadOnly = false
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"reasonForTheOverpaymentOrWriteOff.${ReasonForTheOverpaymentOrWriteOff.DependentNoLongerQualifiedForPension}"))
        )
      )

      ReasonForTheOverpaymentOrWriteOffSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
        SummaryListRow(
          key = "reasonForTheOverpaymentOrWriteOff.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", ReasonForTheOverpaymentOrWriteOffPage(0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("reasonForTheOverpaymentOrWriteOff.change.hidden"))
            )))
          }
        )
      )
    }

    "must display correct information for reason for the overpayment/write off (EmployerOther)" in {

      val answer = UserAnswers().setOrException(ReasonForTheOverpaymentOrWriteOffPage(0), ReasonForTheOverpaymentOrWriteOff.Other)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val isReadOnly = false
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"reasonForTheOverpaymentOrWriteOff.${ReasonForTheOverpaymentOrWriteOff.Other}"))
        )
      )

      ReasonForTheOverpaymentOrWriteOffSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
        SummaryListRow(
          key = "reasonForTheOverpaymentOrWriteOff.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", ReasonForTheOverpaymentOrWriteOffPage(0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("reasonForTheOverpaymentOrWriteOff.change.hidden"))
            )))
          }
        )
      )
    }
  }
}
