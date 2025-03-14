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

package controllers.event1

import base.SpecBase
import models.enumeration.EventType.Event1
import pages.EmptyWaypoints
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event1.WhatYouWillNeedView

class WhatYouWillNeedControllerSpec extends SpecBase {

  private val waypoints = EmptyWaypoints

  "WhatYouWillNeed Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.WhatYouWillNeedController.onPageLoad(waypoints, 0).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatYouWillNeedView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view(controllers.common.routes.MembersDetailsController.onPageLoad(waypoints, Event1, 0, memberPageNo = 0).url)(request, messages(application)).toString
      }
    }

    "must return BAD_REQUEST for invalid index" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.WhatYouWillNeedController.onPageLoad(waypoints, -1).url)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}