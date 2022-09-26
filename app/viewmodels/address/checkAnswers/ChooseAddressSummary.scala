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

package viewmodels.address.checkAnswers

import models.UserAnswers
import pages.{CheckAnswersPage, Waypoints}
import pages.address.ChooseAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ChooseAddressSummary  {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, urlFragment: String)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ChooseAddressPage(urlFragment: String)).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"chooseAddress.$answer"))
          )
        )

        SummaryListRowViewModel(
          key     = "chooseAddress.checkYourAnswersLabel",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", ChooseAddressPage(urlFragment: String).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("chooseAddress.change.hidden"))
          )
        )
    }
}
