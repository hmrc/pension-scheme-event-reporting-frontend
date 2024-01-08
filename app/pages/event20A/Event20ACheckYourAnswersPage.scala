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

package pages.event20A

import models.enumeration.EventType.Event20A
import pages.{CheckAnswersPage, Waypoints}
import play.api.mvc.Call

case class Event20ACheckYourAnswersPage() extends CheckAnswersPage {
  override val urlFragment: String =
    s"event-${Event20A.toString}-check-answers"

  override def route(waypoints: Waypoints): Call = {
    controllers.event20A.routes.Event20ACheckYourAnswersController.onPageLoad
  }

  override def toString: String = "CheckYourAnswersPage"
}


