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

package viewmodels.event1.employer.checkAnswers

import models.UserAnswers
import models.event1.employer.PaymentNature
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.Event1CheckYourAnswersPage
import pages.event1.employer.PaymentNaturePage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, SummaryListRow}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class PaymentNatureSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  "row" - {

    "must display correct information for Loans to or in respect of the employer exceeding 50% option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage(0), PaymentNature.LoansExceeding50PercentOfFundValue)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val isReadOnly = false

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.LoansExceeding50PercentOfFundValue}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
        SummaryListRow(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", PaymentNaturePage(0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
            )))
          }
        )
      )
    }

    "must display correct information for Residential property held directly or indirectly by an investment option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage(0), PaymentNature.ResidentialProperty)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val isReadOnly = false
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.ResidentialProperty}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
        SummaryListRow(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", PaymentNaturePage(0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
            )))
          }
        )
      )
    }

    "must display correct information for Tangible moveable property held directly or indirectly option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage(0), PaymentNature.TangibleMoveableProperty)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val isReadOnly = false
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.TangibleMoveableProperty}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
        SummaryListRow(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", PaymentNaturePage(0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
            )))
          }
        )
      )
    }

    "must display correct information for Court order payment/confiscation order option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage(0), PaymentNature.CourtOrder)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val isReadOnly = false
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.CourtOrder}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
        SummaryListRow(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", PaymentNaturePage(0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
            )))
          }
        )
      )
    }

    "must display correct information for EmployerOther option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage(0), PaymentNature.EmployerOther)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val isReadOnly = false
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.EmployerOther}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
        SummaryListRow(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", PaymentNaturePage(0).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
            )))
          }
        )
      )
    }
  }
}
