package controllers

import base.SpecBase
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{mock, reset}
import connectors.UserAnswersCacheConnector
import forms.{$className$FormProvider, TestFormProvider}
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{mock, reset}
import org.scalatest.BeforeAndAfterEach
import pages.{$className$Page, EmptyWaypoints, TestPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.$className$View

import scala.concurrent.Future

class $className$ControllerSpec extends SpecBase with BeforeAndAfterEach  {

  private val waypoints = EmptyWaypoints

  private val formProvider = new $className$FormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.$className$Controller.onPageLoad(waypoints).url
  private def postRoute: String = routes.$className$Controller.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "$className$ Controller" - {


    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
      }
    }


    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers().set($className$Page, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[TestView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = emptyUserAnswers.set($className$Page, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual $className$Page.navigate(waypoints, emptyUserAnswers, expectedAnswers).url
      }
    }
  }
}
