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

import models.UserAnswers
import pages.event1.member.MemberTangibleMoveablePropertyPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MemberTangibleMoveablePropertySummary {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] = {

    val maybeTangibleDescriptionDefined = answers.get(MemberTangibleMoveablePropertyPage)

    val value = maybeTangibleDescriptionDefined match {
      case Some(_) =>
        ValueViewModel(HtmlFormat.escape(maybeTangibleDescriptionDefined.get).toString)
      case None =>
        ValueViewModel("")
    }

    Some(
      SummaryListRowViewModel(
        key = "memberTangibleMoveableProperty.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", MemberTangibleMoveablePropertyPage.changeLink(waypoints, sourcePage).url)
            .withVisuallyHiddenText(messages("memberTangibleMoveableProperty.change.hidden"))
        )
      )
    )
  }
}
