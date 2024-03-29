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

package viewmodels.event24.checkAnswers

import models.{Index, UserAnswers}
import pages.{CheckAnswersPage, Waypoints}
import pages.event24.EmployerPayeReferencePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object EmployerPayeReferenceSummary  {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(EmployerPayeReferencePage(index)).map {
      answer =>

        SummaryListRowViewModel(
          key     = "employerPayeReference.event24.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", EmployerPayeReferencePage(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("employerPayeReference.event24.change.hidden"))
          )
        )
    }
}
