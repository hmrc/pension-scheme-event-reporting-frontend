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

package viewmodels.event14.checkAnswers

import models.TaxYear.getSelectedTaxYearAsString
import models.UserAnswers
import pages.event14.HowManySchemeMembersPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object HowManySchemeMembersSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HowManySchemeMembersPage).map {
      answer =>
        val taxYearEnd = getSelectedTaxYearAsString(answers)
        val taxYearRange = s"${taxYearEnd.toInt - 1} to $taxYearEnd"

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"howManySchemeMembers.${answer.toString.replaceAll("[, ]", "")}"))
          )
        )

        SummaryListRow(
          key = messages("howManySchemeMembers.checkYourAnswersLabel", taxYearRange),
          value = value,
          actions = if(isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", HowManySchemeMembersPage.changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("howManySchemeMembers.change.hidden"))
            )))
          }
        )
    }
}
