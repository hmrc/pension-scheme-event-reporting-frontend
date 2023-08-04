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

package viewmodels.event10.checkAnswers

import forms.mappings.Formatters
import models.UserAnswers
import models.event10.BecomeOrCeaseScheme
import pages.event10.{BecomeOrCeaseSchemePage, SchemeChangeDatePage}
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SchemeChangeDateSummary extends Formatters {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isChangeLinkNotPresent: Boolean)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SchemeChangeDatePage).map {
      answer =>

        val becameRegulatedScheme = BecomeOrCeaseScheme.ItBecameAnInvestmentRegulatedPensionScheme.toString
        val becomeOrCeased = answers.get(BecomeOrCeaseSchemePage) match {
          case Some(value) => value.toString
          case _ => "This has not been completed. Select ‘Change’ to update"
        }

        val dateCYALabel = if (becomeOrCeased == becameRegulatedScheme) "became.schemeChangeDate.checkYourAnswersLabel" else "ceased.schemeChangeDate.checkYourAnswersLabel"
        val dateCYAHiddenLabel = if (becomeOrCeased == becameRegulatedScheme) "became.schemeChangeDate.change.hidden" else "ceased.schemeChangeDate.change.hidden"


        SummaryListRow(
          key = dateCYALabel,
          value = ValueViewModel(dateFormatter.format(answer.schemeChangeDate)),
          actions = if (isChangeLinkNotPresent) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", SchemeChangeDatePage.changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages(dateCYAHiddenLabel))
            )))
          }
        )
    }
}
