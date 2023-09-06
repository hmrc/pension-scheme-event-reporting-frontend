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

package viewmodels.event6.checkAnswers

import models.enumeration.EventType.Event6
import models.{Index, UserAnswers}
import pages.event6.TypeOfProtectionPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TypeOfProtectionSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage, isReadOnly: Boolean)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TypeOfProtectionPage(Event6, index)).map {
      answer =>
        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"typeOfProtection.$answer"))
          )
        )

        SummaryListRow(
          key = "typeOfProtection.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
            ActionItemViewModel("site.change", TypeOfProtectionPage(Event6, index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("typeOfProtection.change.hidden"))
          )))
          }
        )
    }
}
