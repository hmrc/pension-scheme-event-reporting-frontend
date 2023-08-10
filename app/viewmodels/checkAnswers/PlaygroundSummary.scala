package viewmodels.checkAnswers

import java.time.format.DateTimeFormatter
import models.UserAnswers
import pages.{CheckAnswersPage, PlaygroundPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PlaygroundSummary  {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PlaygroundPage).map {
      answer =>

        val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

        SummaryListRowViewModel(
          key     = "playground.checkYourAnswersLabel",
          value   = ValueViewModel(answer.format(dateFormatter)),
          actions = Seq(
            ActionItemViewModel("site.change", PlaygroundPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("playground.change.hidden"))
          )
        )
    }
}
