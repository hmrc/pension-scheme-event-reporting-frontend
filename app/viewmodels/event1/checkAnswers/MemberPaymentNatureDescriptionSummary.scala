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

package viewmodels.event1.checkAnswers

import models.{Index, UserAnswers}
import pages.event1.member.MemberPaymentNatureDescriptionPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MemberPaymentNatureDescriptionSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] = {

    val maybeOtherDescDefined = answers.get(MemberPaymentNatureDescriptionPage(index))

    val value = maybeOtherDescDefined match {
      case Some(_) =>
        ValueViewModel(HtmlFormat.escape(maybeOtherDescDefined.get).toString)
      case None =>
        ValueViewModel("")
    }

    Some(
      SummaryListRowViewModel(
        key = "memberPaymentNatureDescription.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", MemberPaymentNatureDescriptionPage(index).changeLink(waypoints, sourcePage).url)
            .withVisuallyHiddenText(messages("memberPaymentNatureDescription.change.hidden"))
        )
      )
    )
  }
}
