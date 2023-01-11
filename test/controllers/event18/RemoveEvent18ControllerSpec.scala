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

package controllers.event18

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event18.RemoveEvent18FormProvider
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import pages.event18.{Event18ConfirmationPage, RemoveEvent18Page}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import views.html.event18.RemoveEvent18View

import scala.concurrent.Future

class RemoveEvent18ControllerSpec extends SpecBase with BeforeAndAfterEach  {

  private val waypoints = EmptyWaypoints

  private val formProvider = new RemoveEvent18FormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.RemoveEvent18Controller.onPageLoad(waypoints).url
  private def postRoute: String = routes.RemoveEvent18Controller.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "RemoveEvent18 Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveEvent18View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
      }
    }

    "must update event18Confirmation in userAnswers to false when selecting true" in {
      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockUserAnswersCacheConnector.save(any(), any(), uaCaptor.capture())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        uaCaptor.getValue.get(Event18ConfirmationPage) mustBe Some(false)
        redirectLocation(result).value mustEqual RemoveEvent18Page.navigate(waypoints, emptyUserAnswers, uaCaptor.getValue).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must not change anything in userAnswer and not call user-cache connector when selecting false" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RemoveEvent18Page.navigate(waypoints, emptyUserAnswers, emptyUserAnswers).url
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[RemoveEvent18View]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
