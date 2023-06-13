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

package viewmodels.event19.checkAnswers

import java.time.format.DateTimeFormatter
import models.UserAnswers
import pages.{CheckAnswersPage, Waypoints}
import pages.event19.DateChangeMadePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DateChangeMadeSummary  {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DateChangeMadePage).map {
      answer =>

        val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

        SummaryListRowViewModel(
          key     = "event19.dateChangeMade.checkYourAnswersLabel",
          value   = ValueViewModel(answer.format(dateFormatter)),
          actions = Seq(
            ActionItemViewModel("site.change", DateChangeMadePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("event19.dateChangeMade.change.hidden"))
          )
        )
    }
}
