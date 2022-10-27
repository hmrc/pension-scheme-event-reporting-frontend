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

import models.UserAnswers
import models.enumeration.EventType.Event1
import models.event1.PaymentNature
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.member.PaymentNaturePage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class PaymentNatureSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  "row" - {

    "must display correct information for benefit in kind option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage, PaymentNature.BenefitInKind)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.BenefitInKind}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", PaymentNaturePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
          )
        )
      )
    }

    "must display correct information for Transfer to non-registered pension scheme option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage, PaymentNature.TransferToNonRegPensionScheme)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.TransferToNonRegPensionScheme}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", PaymentNaturePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
          )
        )
      )
    }

    "must display correct information for Error in calculating tax-free lump sums option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage, PaymentNature.ErrorCalcTaxFreeLumpSums)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.ErrorCalcTaxFreeLumpSums}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", PaymentNaturePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
          )
        )
      )
    }

    "must display correct information for Benefits paid early option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage, PaymentNature.BenefitsPaidEarly)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.BenefitsPaidEarly}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", PaymentNaturePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
          )
        )
      )
    }

    "must display correct information for Refund of contributions option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage, PaymentNature.RefundOfContributions)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.RefundOfContributions}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", PaymentNaturePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
          )
        )
      )
    }

    "must display correct information for Overpayment/write off for reasons including death option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage, PaymentNature.OverpaymentOrWriteOff)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.OverpaymentOrWriteOff}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", PaymentNaturePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
          )
        )
      )
    }

    "must display correct information for Residential property held option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage, PaymentNature.ResidentialPropertyHeld)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.ResidentialPropertyHeld}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", PaymentNaturePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
          )
        )
      )
    }

    "must display correct information for Tangible moveable property option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage, PaymentNature.TangibleMoveablePropertyHeld)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.TangibleMoveablePropertyHeld}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", PaymentNaturePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
          )
        )
      )
    }

    "must display correct information for Court order payment/confiscation order option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage, PaymentNature.CourtOrConfiscationOrder)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.CourtOrConfiscationOrder}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", PaymentNaturePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
          )
        )
      )
    }

    "must display correct information for EmployerOther option" in {

      val answer = UserAnswers().setOrException(PaymentNaturePage, PaymentNature.MemberOther)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"paymentNature.${PaymentNature.MemberOther}"))
        )
      )

      PaymentNatureSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentNature.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", PaymentNaturePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentNature.change.hidden"))
          )
        )
      )
    }
  }
}
