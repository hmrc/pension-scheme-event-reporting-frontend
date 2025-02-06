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

package viewmodels.event24.checkAnswers

import models.event24.TypeOfProtectionGroup1
import models.event24.TypeOfProtectionGroup1.{NonResidenceEnhancement, PensionCreditsPreCRE, PreCommencement, SchemeSpecific}
import models.{Index, UserAnswers}
import pages.event24.TypeOfProtectionGroup1ReferencePage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import scala.annotation.tailrec

object EnhancementsSummary {
  def row(protectionTypes: Set[TypeOfProtectionGroup1],
          answers: UserAnswers,
          waypoints: Waypoints,
          index: Index,
          sourcePage: CheckAnswersPage,
          isReadOnly: Boolean)
         (implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(TypeOfProtectionGroup1ReferencePage(index)).map {
      answer =>

        @tailrec
        def aux(protectionTypes: Set[TypeOfProtectionGroup1], content: String): String = {
          if (protectionTypes.isEmpty) {
            content
          } else if (protectionTypes.head == NonResidenceEnhancement) {
            val newContent = content +
              getContent(messages("nonResidenceEnhancement.event24.checkYourAnswersLabel"), answer.nonResidenceEnhancement) + addPageBreak(protectionTypes.tail)
            aux(protectionTypes.tail, newContent)
          } else if (protectionTypes.head == PensionCreditsPreCRE) {
            val newContent = content +
              getContent(messages("pensionCreditsPreCRE.event24.checkYourAnswersLabel"), answer.pensionCreditsPreCRE) + addPageBreak(protectionTypes.tail)
            aux(protectionTypes.tail, newContent)
          } else if (protectionTypes.head == PreCommencement) {
            val newContent = content +
              getContent(messages("preCommencement.event24.checkYourAnswersLabel"), answer.preCommencement) + addPageBreak(protectionTypes.tail)
            aux(protectionTypes.tail, newContent)
          } else {
            val newContent = content +
              getContent(messages("recognisedOverseasPSTE.event24.checkYourAnswersLabel"), answer.recognisedOverseasPSTE) + addPageBreak(protectionTypes.tail)
            aux(protectionTypes.tail, newContent)
          }
        }

        SummaryListRow(
          key     = "references.event24.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(aux(protectionTypes - SchemeSpecific, ""))),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", TypeOfProtectionGroup1ReferencePage(index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("typeOfProtectionReferenceGroup1.event24.change.hidden"))
            )))
          }
        )
    }
  }

  private def getContent(title: String, reference: String) = {
      s"""<p class="govuk-body">${title}</p>
         |<p class="govuk-body">${reference}</p>""".stripMargin
  }

  private def addPageBreak(protectionTypes: Set[TypeOfProtectionGroup1]) = {
    if (protectionTypes.nonEmpty) {
      "</br>"
    } else {
      ""
    }
  }
}
