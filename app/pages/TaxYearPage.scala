/*
 * Copyright 2023 HM Revenue & Customs
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
import models.enumeration.JourneyStartType.{InProgress, PastEventTypes, StartNew}
import models.enumeration.VersionStatus.NotStarted
import models.{TaxYear, UserAnswers, VersionInfo}
import pages.amend.ReturnHistoryPage
import play.api.libs.json.JsPath
import play.api.mvc.Call
case object TaxYearPage extends QuestionPage[TaxYear] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "taxYear"

  override def route(waypoints: Waypoints): Call =
    routes.TaxYearController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    (answers.get(EventReportingTileLinksPage), answers.get(VersionInfoPage)) match {
      case (Some(InProgress), _) => EventSummaryPage
      case (Some(StartNew), Some(VersionInfo(_, NotStarted)) | None) => EventSelectionPage
      case (Some(StartNew), _) => EventSummaryPage
      case (Some(PastEventTypes), _) => ReturnHistoryPage
      case _ => JourneyRecoveryPage
    }
  }
}
