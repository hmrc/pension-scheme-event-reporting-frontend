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
import models.enumeration.EventType
import models.enumeration.EventType.Event18
import play.api.mvc.Call

object CheckYourAnswersPage {

  def apply(eventType: EventType): CheckAnswersPage = new CheckAnswersPage {

    override val urlFragment: String = s"event-${eventType.toString}-check-answers"

    override def route(waypoints: Waypoints): Call = {
      routes.CheckYourAnswersController.onPageLoad(eventType)
    }
  }

  val event18: CheckAnswersPage = CheckYourAnswersPage(Event18)
}
