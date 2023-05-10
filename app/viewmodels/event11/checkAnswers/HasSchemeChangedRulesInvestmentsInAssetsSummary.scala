/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels.checkAnswers

import models.UserAnswers
import pages.{CheckAnswersPage, Waypoints}
import pages.event11.{HasSchemeChangedRulesInvestmentsInAssetsPage, InvestmentsInAssetsRuleChangeDatePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.format.DateTimeFormatter

object HasSchemeChangedRulesInvestmentsInAssetsSummary  {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HasSchemeChangedRulesInvestmentsInAssetsPage).map {
      answer =>

        val value = if (answer) {
          // TODO: fix so date on new line.
          s"Yes \n\n\n\n\n\n\n\n\n\n ${answers.get(InvestmentsInAssetsRuleChangeDatePage).get.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
        } else {
          "The scheme has not changed its rules to allow investments in assets other than contracts or policies of insurance"
        }

        SummaryListRowViewModel(
          //LDS ignore
          key     = "hasSchemeChangedRulesInvestmentsInAssets.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", HasSchemeChangedRulesInvestmentsInAssetsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("hasSchemeChangedRulesInvestmentsInAssets.change.hidden"))
          )
        )
    }
}
