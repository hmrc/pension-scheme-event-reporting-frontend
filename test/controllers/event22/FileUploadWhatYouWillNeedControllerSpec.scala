package controllers.event22

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event22.FileUploadWhatYouWillNeedView
import controllers.event22.routes._

class FileUploadWhatYouWillNeedControllerSpec extends SpecBase {

  "FileUploadWhatYouWillNeed Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.FileUploadWhatYouWillNeedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadWhatYouWillNeedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
