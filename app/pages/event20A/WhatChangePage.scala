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

package pages.event20A

import controllers.event20A.routes
import models.UserAnswers
import models.event20A.WhatChange
import models.event20A.WhatChange.{BecameMasterTrust, CeasedMasterTrust}
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.{Success, Try}

case object WhatChangePage extends QuestionPage[WhatChange] {

  override def path: JsPath = JsPath \ "event20A" \ toString

  override def toString: String = "whatChange"

  override def route(waypoints: Waypoints): Call =
    routes.WhatChangeController.onPageLoad(waypoints)

  override def cleanupBeforeSettingValue(value: Option[WhatChange], userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.get(this) match {
      case originalOption@Some(_) if originalOption != value =>
        userAnswers.remove(this)
      case _ => Success(userAnswers)
    }
  }

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this).map {
      case BecameMasterTrust => BecameDatePage
      case CeasedMasterTrust => CeasedDatePage
    }.orRecover
  }

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    val originalOptionSelected = originalAnswers.get(this)
    val updatedOptionSelected = updatedAnswers.get(this)
    val answerIsChanged = originalOptionSelected != updatedOptionSelected
    (answerIsChanged, updatedOptionSelected) match {
      case (true, Some(BecameMasterTrust)) => BecameDatePage
      case (true, Some(CeasedMasterTrust)) => CeasedDatePage
      case _ => Event20ACheckYourAnswersPage()
    }
  }
}
