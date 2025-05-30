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

import models.{Index, UserAnswers}
import pages.event1.employer.CompanyDetailsPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CompanyDetailsSummary {

  def rowCompanyDetails(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage, isReadOnly: Boolean)
                       (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CompanyDetailsPage(index)).map {
      answer =>

        val htmlContent = HtmlContent(
          s"""<p class="govuk-body">${answer.companyName}</p>
             |<p class="govuk-body">${answer.companyNumber}</p>""".stripMargin)

        SummaryListRow(
          key = "companyDetails.title",
          value = ValueViewModel(htmlContent),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", CompanyDetailsPage(index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("companyDetails.title"))
            )))
          }
        )
    }
}
