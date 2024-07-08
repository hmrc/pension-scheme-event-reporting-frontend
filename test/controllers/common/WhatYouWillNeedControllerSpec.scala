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

package controllers.common

import base.SpecBase
import models.enumeration.EventType
import pages.EmptyWaypoints
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.common.WhatYouWillNeedView

class WhatYouWillNeedControllerSpec extends SpecBase {

  private val waypoints = EmptyWaypoints

  "WhatYouWillNeed Controller" - {
    testGetCorrectPageViewforEvent(EventType.Event3)
    testGetCorrectPageViewforEvent(EventType.Event4)
    testGetCorrectPageViewforEvent(EventType.Event5)
    testGetCorrectPageViewforEvent(EventType.Event7)
    testGetCorrectPageViewforEvent(EventType.Event8)
    testGetCorrectPageViewforEvent(EventType.Event8A)
  }

  private def testGetCorrectPageViewforEvent(eventType: EventType): Unit = {
    s"must return OK and the correct view for Event $eventType" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.WhatYouWillNeedController.onPageLoad(waypoints, eventType, 0).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatYouWillNeedView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual
          view(eventType, controllers.common.routes.MembersDetailsController.onPageLoad(waypoints, eventType,
            0, memberPageNo = 0).url)(request, messages(application)).toString
      }
    }
  }
}
