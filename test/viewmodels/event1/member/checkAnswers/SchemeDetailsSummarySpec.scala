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

import data.SampleData.schemeDetails
import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.Event1CheckYourAnswersPage
import pages.event1.member.SchemeDetailsPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class SchemeDetailsSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  private def schemeDetailsAnswer(detail: Option[String]): Html = {
    def schemeDetailsToHtml(value: String): String = s"$value"

    def optionalDetailToHtml(optionalSchemeDetail: Option[String]): String = optionalSchemeDetail match {
      case Some(schemeDet) => schemeDetailsToHtml(schemeDet)
      case None => ""
    }

    Html(
      optionalDetailToHtml(detail)
    )
  }

  "rowSchemeName" - {

    "must display correct information for Scheme Name" in {

      val answer = UserAnswers().setOrException(SchemeDetailsPage(0), schemeDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      val value = ValueViewModel(
        HtmlContent(
          schemeDetailsAnswer(schemeDetails.schemeName)
        )
      )

      SchemeDetailsSummary.rowSchemeName(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "Scheme name",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", SchemeDetailsPage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("schemeDetails.change.hidden"))
          )
        )
      )
    }
  }

  "rowSchemeReference" - {

    "must display correct information for Scheme Reference" in {

      val answer = UserAnswers().setOrException(SchemeDetailsPage(0), schemeDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      val value = ValueViewModel(
        HtmlContent(
          schemeDetailsAnswer(schemeDetails.reference)
        )
      )

      SchemeDetailsSummary.rowSchemeReference(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "Reference",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", SchemeDetailsPage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("schemeReference.change.hidden"))
          )
        )
      )
    }
  }
}
