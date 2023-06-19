package controllers.event20A

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event20A.Event20APsaDeclarationView
import controllers.event20A.routes._

class Event20APsaDeclarationControllerSpec extends SpecBase {

  "Event20APsaDeclaration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.Event20APsaDeclarationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[Event20APsaDeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
