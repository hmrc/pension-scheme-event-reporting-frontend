package viewmodels.checkAnswers

import java.time.format.DateTimeFormatter
import models.UserAnswers
import pages.{$className$Page, CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object $className$Summary  {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get($className$Page).map {
      answer =>

        val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

        SummaryListRowViewModel(
          key     = "$className;format="decap"$.checkYourAnswersLabel",
          value   = ValueViewModel(answer.format(dateFormatter)),
          actions = Seq(
            ActionItemViewModel("site.change", $className$Page.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("$className;format="decap"$.change.hidden"))
          )
        )
    }
}
