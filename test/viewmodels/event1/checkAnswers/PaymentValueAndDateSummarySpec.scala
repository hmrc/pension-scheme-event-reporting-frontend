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

import base.SpecBase
import models.UserAnswers
import models.enumeration.EventType.Event1
import models.event1.PaymentDetails
import pages.event1.PaymentValueAndDatePage
import pages.event1.member.BenefitInKindBriefDescriptionPage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.twirl.api.HtmlFormat
import viewmodels.checkAnswers.PaymentValueAndDateSummary
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

import java.time.LocalDate


class PaymentValueAndDateSummarySpec extends SpecBase with SummaryListFluency {


  "row" - {

    /*
      Some(SummaryListRow(Key(Text(Payment value and date),),Value(Text(PaymentDetails(1000.0,2022-10-24)),),,Some(Actions(,List(ActionItem(/new-report/event-1-payment-details?waypoints=event-1-check-answers,Text(Change),Some(Payment value and date),,Map()))))))
      Some(SummaryListRow(Key(Text(Payment value and date),),Value(Text(PaymentDetails(1000.0,2022-10-24)),),,Some(Actions(,List(ActionItem(/new-report/event-1-benefit-in-kind?waypoints=event-1-check-answers,Text(Change),Some(Payment value and date),,Map()))))))
    */

    "must display correct information" in {
      val paymentDetails = PaymentDetails(1000.00, LocalDate.now())

      val answer = UserAnswers().setOrException(PaymentValueAndDatePage, paymentDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      PaymentValueAndDateSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "paymentValueAndDate.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape(paymentDetails.toString).toString),
          actions = Seq(
            ActionItemViewModel("site.change", PaymentValueAndDatePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentValueAndDate.checkYourAnswersLabel"))
          )
        )
      )
    }
  }
}
