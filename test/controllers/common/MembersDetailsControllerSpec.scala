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
import connectors.UserAnswersCacheConnector
import forms.common.MembersDetailsFormProvider
import models.Index
import models.common.MembersDetails
import models.enumeration.EventType
import models.enumeration.EventType._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.common.MembersDetailsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.common.MembersDetailsView

import scala.concurrent.Future

class MembersDetailsControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val seqOfEvents = Seq(Event1, Event3, Event4, Event5, Event6, Event7, Event8, Event8A, Event22, Event23)
  private val validValue = MembersDetails("Joe", "Blogs", "AA123456D")
  private val formProvider = new MembersDetailsFormProvider()
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private def form(eventType: EventType): Form[MembersDetails] = formProvider(eventType)

  private def getRoute(eventType: EventType): String = routes.MembersDetailsController.onPageLoad(waypoints, eventType, Index(0), memberPageNo = 0).url

  private def postRoute(eventType: EventType): String = routes.MembersDetailsController.onSubmit(waypoints, eventType, 0, memberPageNo = 0).url

  private def submitUrl(eventType: EventType): Call = controllers.common.routes.MembersDetailsController.onSubmit(waypoints, eventType, 0, memberPageNo = 0)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "MembersDetails Controller" - {
    for (event <- seqOfEvents) {
      testSuite(event)
    }
  }

  private def testSuite(eventType: EventType): Unit = {
    testReturnOkAndCorrectView(eventType)
    testPopulateCorrectViewOnGetWhenPrevAnswered(eventType)
    testSaveAnswerAndRedirectWhenValid(eventType)
    testBadRequestForInvalidDataSubmission(eventType)
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))
        val result = route(application, request).value
        val view = application.injector.instanceOf[MembersDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(
          form(eventType),
          waypoints,
          eventType,
          memberPageNo = 0,
          submitUrl = submitUrl(eventType), request = request, messages = messages(application)).toString
      }
    }
  }

  private def testPopulateCorrectViewOnGetWhenPrevAnswered(eventType: EventType): Unit = {
    s"must populate the view correctly on a GET when the question has previously been answered for Event $eventType" in {
      val userAnswers = emptyUserAnswersWithTaxYear.set(MembersDetailsPage(eventType, 0), validValue).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))
        val view = application.injector.instanceOf[MembersDetailsView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(
          form(eventType).fill(validValue),
          waypoints,
          eventType,
          memberPageNo = 0,
          submitUrl = submitUrl(eventType),
          request = request, messages = messages(application)).toString
      }
    }
  }

  private def testSaveAnswerAndRedirectWhenValid(eventType: EventType): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for Event $eventType" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(
            ("firstName", validValue.firstName), ("lastName", validValue.lastName), ("nino", validValue.nino)
          )

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswersWithTaxYear.set(MembersDetailsPage(eventType, 0), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MembersDetailsPage(eventType, 0).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }
  }

  //scalastyle:off method.length
  private def testBadRequestForInvalidDataSubmission(eventType: EventType): Unit = {
    s"must return bad request when invalid data is submitted for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("firstName", "%"), ("lastName", ""), ("nino", "abc"))
        val view = application.injector.instanceOf[MembersDetailsView]
        val boundForm = form(eventType).bind(Map("firstName" -> "%", "lastName" -> "", "nino" -> "abc"))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view.render(
          boundForm,
          waypoints,
          eventType,
          memberPageNo = 0,
          submitUrl(eventType),
          request,
          messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }

    s"must return bad request when invalid data (last name is empty) is submitted for Event $eventType - when member exists" in {

      val nino = "CS121212C"
      val ua = emptyUserAnswersWithTaxYear.setOrException(MembersDetailsPage(eventType, 0), MembersDetails("firstName", "lastName", nino))

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("firstName", "John"), ("lastName", ""), ("nino", nino))

        val view = application.injector.instanceOf[MembersDetailsView]
        val boundForm = form(eventType).bind(Map("firstName" -> "John", "lastName" -> "", "nino" -> nino))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view.render(
          boundForm,
          waypoints,
          eventType,
          memberPageNo = 0,
          submitUrl(eventType),
          request,
          messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
