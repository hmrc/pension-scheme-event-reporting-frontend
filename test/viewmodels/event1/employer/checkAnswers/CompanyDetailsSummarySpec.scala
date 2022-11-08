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

package viewmodels.event1.employer.checkAnswers

import data.SampleData.companyDetails
import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.Event1CheckYourAnswersPage
import pages.event1.employer.CompanyDetailsPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class CompanyDetailsSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  "rowCompanyName" - {

    "must display correct information" in {

      val answer = UserAnswers().setOrException(CompanyDetailsPage(0), companyDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      CompanyDetailsSummary.rowCompanyName(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "companyDetails.CYA.companyName",
          value = ValueViewModel(HtmlFormat.escape(companyDetails.companyName).toString),
          actions = Seq(
            ActionItemViewModel("site.change", CompanyDetailsPage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("companyDetails.companyName.change.hidden"))
          )
        )
      )
    }
  }


  "rowCompanyNumber" - {

    "must display correct information" in {

      val answer = UserAnswers().setOrException(CompanyDetailsPage(0), companyDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      CompanyDetailsSummary.rowCompanyNumber(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "companyDetails.CYA.companyNumber",
          value = ValueViewModel(HtmlFormat.escape(companyDetails.companyNumber).toString),
          actions = Seq(
            ActionItemViewModel("site.change", CompanyDetailsPage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("companyDetails.companyNumber.change.hidden"))
          )
        )
      )
    }
  }
}
