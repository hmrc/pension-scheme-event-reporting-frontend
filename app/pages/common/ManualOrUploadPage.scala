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

package pages.common

import controllers.common.routes
import models.UserAnswers
import models.common.ManualOrUpload
import models.common.ManualOrUpload.{FileUpload, Manual}
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event23, Event6, Event8}
import pages.event1.WhoReceivedUnauthPaymentPage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class ManualOrUploadPage(eventType: EventType, index: Int) extends QuestionPage[ManualOrUpload] {

  override def path: JsPath =
    eventType match {
      case Event6 | Event8 | Event22 | Event23 => MembersPage(eventType)(index) \ toString
      case _ => MembersOrEmployersPage(eventType)(index) \ toString
    }

  override def toString: String = "manualOrUpload"

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    (eventType, index, answers.get(this)) match {
      case (Event1, index, Some(Manual)) => WhoReceivedUnauthPaymentPage(index)
      case (Event6, index, Some(Manual)) => pages.common.MembersDetailsPage(Event6, index)
      case (Event6, _, Some(FileUpload)) => pages.common.FileUploadWhatYouWillNeedPage(eventType)
      case (Event22, index, Some(Manual)) => pages.event22.WhatYouWillNeedPage(index)
      case (Event22, _, Some(FileUpload)) => pages.common.FileUploadWhatYouWillNeedPage(eventType)
      case (Event23, _, Some(FileUpload)) => pages.common.FileUploadWhatYouWillNeedPage(eventType)
      case (Event23, index, Some(Manual)) => pages.event23.WhatYouWillNeedPage(index)
      case _ => this
    }

  override def route(waypoints: Waypoints): Call = {
    routes.ManualOrUploadController.onPageLoad(waypoints, eventType, index)
  }
}
