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

package pages

import controllers.routes
import models.UserAnswers
import models.requests.DataRequest
import play.api.libs.json.JsPath
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Key, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._

case object TestYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "test"

  override def route(waypoints: Waypoints): Call =
    routes.TestYesNoController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this).map {
      case true  =>
        CheckYourAnswersPage
      case false => TestYesNoPage
    }.orRecover
  }

  def cya(waypoints: Waypoints)(implicit request: DataRequest[AnyContent]): Option[SummaryListRow] = {
    request.userAnswers.get(this) map { answer =>
        SummaryListRowViewModel(
          key = Key(Text("test.checkYourAnswersLabel")),
          value = ValueViewModel(Text(answer.toString)),
          actions = Seq(
            ActionItem("site.change", Text(TestYesNoPage.changeLink(waypoints, CheckYourAnswersPage).url))
          )
        )
    }
  }
}
