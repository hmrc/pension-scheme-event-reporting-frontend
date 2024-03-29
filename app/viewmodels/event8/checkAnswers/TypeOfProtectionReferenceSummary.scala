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

package viewmodels.event8.checkAnswers

import models.UserAnswers
import models.enumeration.EventType
import pages.event8.{TypeOfProtectionPage, TypeOfProtectionReferencePage}
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TypeOfProtectionReferenceSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean, eventType: EventType, index: Int)
         (implicit messages: Messages): Option[SummaryListRow] = {

    val protectionType = answers.get(TypeOfProtectionPage(eventType, index)) match {
      case Some(value) => value
      case _ => None
    }

    answers.get(TypeOfProtectionReferencePage(eventType, index)).map {
      answer =>

        SummaryListRow(
          key = messages(s"typeOfProtectionReference.checkYourAnswersLabel",
            messages(s"event8.typeOfProtection.${protectionType.toString}").toLowerCase()),
          value = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
            ActionItemViewModel("site.change", TypeOfProtectionReferencePage(eventType, index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("typeOfProtectionReference.change.hidden",
                messages(s"event8.typeOfProtection.${protectionType.toString}").toLowerCase()))
          )))
          }
        )
    }
  }
}