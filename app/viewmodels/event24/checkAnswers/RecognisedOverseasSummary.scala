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
import pages.event24.{TypeOfProtectionGroup1Page, TypeOfProtectionGroup1ReferencePage}
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RecognisedOverseasSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage, isReadOnly: Boolean)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TypeOfProtectionGroup1ReferencePage(index)).map {
      answer =>

        val value = answer.recognisedOverseasPSTE

        SummaryListRow(
          key     = "recognisedOverseasPSTE.event24.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", TypeOfProtectionGroup1ReferencePage(index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("typeOfProtectionReferenceGroup1.event24.change.hidden"))
            )))
          }
        )
    }
}
