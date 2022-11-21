package viewmodels.checkAnswers

import models.UserAnswers
import pages.{CheckAnswersPage, Waypoints}
import pages.event18.removeEvent23Page
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object removeEvent23Summary  {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(removeEvent23Page).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "removeEvent23.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", removeEvent23Page.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("removeEvent23.change.hidden"))
          )
        )
    }
}
