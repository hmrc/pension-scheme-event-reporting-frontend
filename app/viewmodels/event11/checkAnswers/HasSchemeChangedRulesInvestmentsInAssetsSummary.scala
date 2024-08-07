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

package viewmodels.event11.checkAnswers

import models.UserAnswers
import pages.event11.HasSchemeChangedRulesInvestmentsInAssetsPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object HasSchemeChangedRulesInvestmentsInAssetsSummary  {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HasSchemeChangedRulesInvestmentsInAssetsPage).map {
      answer =>

        val value = if (answer) {
          "Yes"
        } else {
          "No"
        }

        SummaryListRow(
          //LDS ignore
          key     = "hasSchemeChangedRulesInvestmentsInAssets.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(value)),
          actions = if(isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", HasSchemeChangedRulesInvestmentsInAssetsPage.changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("hasSchemeChangedRulesInvestmentsInAssets.change.hidden"))
            )))
          }
        )
    }
}
