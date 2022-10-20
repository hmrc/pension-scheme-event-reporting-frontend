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

import base.SpecBase
import data.SampleData.companyDetails
import models.UserAnswers
import models.enumeration.EventType.Event1
import pages.event1.employer.CompanyDetailsPage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.twirl.api.HtmlFormat
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class CompanyDetailsSummarySpec extends SpecBase with SummaryListFluency {


  "rowCompanyName" - {

    "must display correct information" in {

      val answer = UserAnswers().setOrException(CompanyDetailsPage, companyDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      CompanyDetailsSummary.rowCompanyName(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "companyDetails.CYA.companyName",
          value = ValueViewModel(HtmlFormat.escape(companyDetails.companyName).toString),
          actions = Seq(
            ActionItemViewModel("site.change", CompanyDetailsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("companyDetails.change.hidden"))
          )
        )
      )
    }
  }


  "rowCompanyNumber" - {

    "must display correct information" in {

      val answer = UserAnswers().setOrException(CompanyDetailsPage, companyDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      CompanyDetailsSummary.rowCompanyNumber(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "companyDetails.CYA.companyNumber",
          value = ValueViewModel(HtmlFormat.escape(companyDetails.companyNumber).toString),
          actions = Seq(
            ActionItemViewModel("site.change", CompanyDetailsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("companyDetails.change.hidden"))
          )
        )
      )
    }
  }
}
