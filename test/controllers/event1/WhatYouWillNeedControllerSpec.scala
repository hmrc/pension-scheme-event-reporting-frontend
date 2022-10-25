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

package controllers.event1

import base.SpecBase
import models.enumeration.EventType
import pages.EmptyWaypoints
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event1.WhatYouWillNeedView

class WhatYouWillNeedControllerSpec extends SpecBase {

  private val waypoints = EmptyWaypoints
  private val event1 = EventType.Event1

  "WhatYouWillNeed Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.WhatYouWillNeedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatYouWillNeedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(controllers.common.routes.MembersDetailsController.onPageLoad(waypoints, event1).url)(request, messages(application)).toString
      }
    }
  }
}
