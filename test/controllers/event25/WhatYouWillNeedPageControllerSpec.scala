package controllers.event25

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event25.WhatYouWillNeedPageView
import controllers.event25.routes._

class WhatYouWillNeedPageControllerSpec extends SpecBase {

  "WhatYouWillNeedPage Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.WhatYouWillNeedPageController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatYouWillNeedPageView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
