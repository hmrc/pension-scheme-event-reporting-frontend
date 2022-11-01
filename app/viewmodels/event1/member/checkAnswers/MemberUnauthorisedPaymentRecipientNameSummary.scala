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

import models.event1.WhoReceivedUnauthPayment.Member
import models.{Index, UserAnswers}
import org.apache.commons.lang3.StringUtils
import pages.event1.WhoReceivedUnauthPaymentPage
import pages.event1.member.UnauthorisedPaymentRecipientNamePage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MemberUnauthorisedPaymentRecipientNameSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] = {

    val valueViewModel = answers.get(UnauthorisedPaymentRecipientNamePage(index)) match {
      case Some(value) =>
        ValueViewModel(HtmlFormat.escape(value).toString)
      case None =>
        ValueViewModel(StringUtils.EMPTY)
    }

    answers.get(WhoReceivedUnauthPaymentPage(index)) match {
      case Some(Member) =>
        Some(
          SummaryListRowViewModel(
            key = "unauthorisedPaymentRecipientName.member.checkYourAnswersLabel",
            value = valueViewModel,
            actions = Seq(
              ActionItemViewModel("site.change", UnauthorisedPaymentRecipientNamePage(index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("unauthorisedPaymentRecipientName.member.change.hidden"))
            )
          )
        )
      case _ => None
    }
  }
}
