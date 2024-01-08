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

package viewmodels.event1.member.checkAnswers

import models.{Index, UserAnswers}
import org.apache.commons.lang3.StringUtils
import pages.event1.member.SchemeDetailsPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SchemeDetailsSummary {

  private def schemeDetailsAnswer(detail: Option[String]): Html = {
    Html(
      detail.getOrElse(StringUtils.EMPTY)
    )
  }

  def rowSchemeName(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage, isReadOnly: Boolean)
                   (implicit messages: Messages): Option[SummaryListRow] = {


    val value = answers.get(SchemeDetailsPage(index)).map {
      answer =>
        ValueViewModel(
          HtmlContent(
            schemeDetailsAnswer(answer.schemeName)
          )
        )
    }

    Some(
      SummaryListRow(
        key = "Scheme name",
        value = value.getOrElse(ValueViewModel(StringUtils.EMPTY)),
        actions = if (isReadOnly) None else {
          Some(Actions(items = Seq(
            ActionItemViewModel("site.change", SchemeDetailsPage(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("schemeName.change.hidden"))
          )))
        }
      )
    )
  }

  def rowSchemeReference(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage, isReadOnly: Boolean)
                        (implicit messages: Messages): Option[SummaryListRow] = {

    val value = answers.get(SchemeDetailsPage(index)).map {
      answer =>
        ValueViewModel(
          HtmlContent(
            schemeDetailsAnswer(answer.reference)
          )
        )
    }

    Some(
      SummaryListRow(
        key = "Reference",
        value = value.getOrElse(ValueViewModel(StringUtils.EMPTY)),
        actions = if (isReadOnly) None else {
          Some(Actions(items = Seq(
            ActionItemViewModel("site.change", SchemeDetailsPage(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("schemeReference.change.hidden"))
          )))
        }
      )
    )
  }
}
