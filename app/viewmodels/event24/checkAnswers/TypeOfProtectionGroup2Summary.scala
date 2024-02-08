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
import pages.event24.TypeOfProtectionGroup2Page
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TypeOfProtectionGroup2Summary {

  def row(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage, isReadOnly: Boolean)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TypeOfProtectionGroup2Page(index)).map {
      answer =>
        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"typeOfProtection.event24.$answer"))
          )
        )

        SummaryListRow(
          key = "typeOfProtection.event24.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
            ActionItemViewModel("site.change", TypeOfProtectionGroup2Page(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("typeOfProtection.event24.change.hidden"))
          )))
          }
        )
    }
}
