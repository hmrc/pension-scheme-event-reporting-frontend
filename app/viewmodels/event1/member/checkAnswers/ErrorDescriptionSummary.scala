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

package viewmodels.event1.member.checkAnswers

import models.{Index, UserAnswers}
import org.apache.commons.lang3.StringUtils
import pages.event1.member.ErrorDescriptionPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ErrorDescriptionSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] = {

    val valueViewModel = answers.get(ErrorDescriptionPage(index)) match {
      case Some(value) =>
        ValueViewModel(HtmlFormat.escape(value).toString)
      case None =>
        ValueViewModel(StringUtils.EMPTY)
    }

    Some(
      SummaryListRowViewModel(
        key = "errorDescription.checkYourAnswersLabel",
        value = valueViewModel,
        actions = Seq(
          ActionItemViewModel("site.change", ErrorDescriptionPage(index).changeLink(waypoints, sourcePage).url)
            .withVisuallyHiddenText(messages("errorDescription.change.hidden"))
        )
      )
    )
  }
}
