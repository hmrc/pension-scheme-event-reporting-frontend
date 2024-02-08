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

import models.UserAnswers
import pages.event24.{TypeOfProtectionGroup2Page, TypeOfProtectionGroup2ReferencePage}
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TypeOfProtectionGroup2ReferenceSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean, index: Int)
         (implicit messages: Messages): Option[SummaryListRow] = {

    val protectionType = answers.get(TypeOfProtectionGroup2Page(index)) match {
      case Some(value) => value
      case _ => None
    }

    answers.get(TypeOfProtectionGroup2ReferencePage(index)).map {
      answer =>

        SummaryListRow(
          key = messages(s"typeOfProtectionReference.event24.checkYourAnswersLabel",
            messages(s"typeOfProtection.event24.${protectionType.toString}").toLowerCase()),
          value = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
            ActionItemViewModel("site.change", TypeOfProtectionGroup2ReferencePage(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("typeOfProtectionReference.event24.change.hidden",
                messages(s"typeOfProtection.event24.${protectionType.toString}").toLowerCase()))
          )))
          }
        )
    }
  }
}