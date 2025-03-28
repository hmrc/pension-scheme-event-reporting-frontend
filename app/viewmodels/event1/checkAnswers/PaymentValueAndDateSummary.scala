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

import forms.mappings.Formatters
import models.{Index, UserAnswers}
import pages.event1.PaymentValueAndDatePage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PaymentValueAndDateSummary extends Formatters {

  def rowPaymentDetails(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage, isReadOnly: Boolean)
                       (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PaymentValueAndDatePage(index)).map {
      answer =>

        val htmlContent = HtmlContent(
          s"""<p class="govuk-body">£${currencyFormatter.format(answer.paymentValue)}</p>
             |<p class="govuk-body">${dateFormatter.format(answer.paymentDate)}</p>""".stripMargin)

        SummaryListRow(
          key = messages("paymentValueAndDate.value.checkYourAnswersLabel"),
          value = ValueViewModel(htmlContent),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", PaymentValueAndDatePage(index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("paymentValueAndDate.value.change.hidden"))
            )))
          }
        )
    }
}
