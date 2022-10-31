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

package viewmodels.event1.employer.checkAnswers

import models.UserAnswers
import models.event1.WhoReceivedUnauthPayment.Employer
import pages.event1.WhoReceivedUnauthPaymentPage
import pages.event1.employer.UnauthorisedPaymentRecipientNamePage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object EmployerUnauthorisedPaymentRecipientNameSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] = {

    answers.get(UnauthorisedPaymentRecipientNamePage).flatMap {
      answer =>
        val value = if (!answer.isBlank) {
          ValueViewModel(HtmlFormat.escape(answer).toString)
        } else {
          ValueViewModel("")
        }

        answers.get(WhoReceivedUnauthPaymentPage) match {
          case Some(Employer) =>
            Some(
              SummaryListRowViewModel(
                key = "unauthorisedPaymentRecipientName.employer.checkYourAnswersLabel",
                value = value,
                actions = Seq(
                  ActionItemViewModel("site.change", UnauthorisedPaymentRecipientNamePage.changeLink(waypoints, sourcePage).url)
                    .withVisuallyHiddenText(messages("unauthorisedPaymentRecipientName.employer.change.hidden"))
                )
              )
            )
          case _ => None
        }
    }
  }
}
