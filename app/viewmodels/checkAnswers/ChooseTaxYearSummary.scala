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

package viewmodels.checkAnswers

import models.TaxYear.getTaxYear
import models.common.ChooseTaxYear
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.{CheckAnswersPage, Waypoints, common}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ChooseTaxYearSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean, eventType: EventType, index: Index)
         (implicit messages: Messages): Option[SummaryListRow] = {
    val taxYearChosen = getTaxYear(answers)
    val rdsTaxYear = ChooseTaxYear.reads(ChooseTaxYear.enumerable(taxYearChosen))
    answers.get(common.ChooseTaxYearPage(eventType, index))(rdsTaxYear).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages("chooseTaxYear.yearRangeRadio", answer, (answer.toString.toInt + 1).toString))
          )
        )

        SummaryListRow(
          key = s"chooseTaxYear.event${eventType.toString}.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", common.ChooseTaxYearPage(eventType, index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages(s"chooseTaxYear.event$eventType.change.hidden"))
            )))
          }
        )
    }
  }
}