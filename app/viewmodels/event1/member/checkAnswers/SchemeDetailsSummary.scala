/*
 * Copyright 2022 HM Revenue & Customs
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
import pages.event1.member.SchemeDetailsPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SchemeDetailsSummary {

  private def schemeDetailsAnswer(detail: Option[String])(implicit messages: Messages): Html = {
    def schemeDetailsToHtml(value: String): String = s"$value"

    def optionalDetailToHtml(optionalSchemeDetail: Option[String]): String = optionalSchemeDetail match {
      case Some(schemeDet) => schemeDetailsToHtml(schemeDet)
      case None => ""
    }

    Html(
      optionalDetailToHtml(detail)
    )
  }

  def rowSchemeName(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                   (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SchemeDetailsPage(index)).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            schemeDetailsAnswer(answer.schemeName)
          )
        )

        SummaryListRowViewModel(
          key = "Scheme name",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", SchemeDetailsPage(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("schemeDetails.change.hidden"))
          )
        )
    }

  def rowSchemeReference(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                        (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SchemeDetailsPage(index)).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            schemeDetailsAnswer(answer.reference)
          )
        )

        SummaryListRowViewModel(
          key = "Reference",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", SchemeDetailsPage(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("schemeDetails.change.hidden"))
          )
        )
    }
}
