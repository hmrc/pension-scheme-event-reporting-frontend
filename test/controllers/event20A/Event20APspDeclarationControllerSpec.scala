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

package controllers.event20A

import base.SpecBase
import connectors.MinimalConnector.{IndividualDetails, MinimalDetails}
import connectors.{EventReportingConnector, MinimalConnector, SchemeDetailsConnector, UserAnswersCacheConnector}
import data.SampleData.sampleEvent20ABecameJourneyData
import forms.event20A.Event20APspDeclarationFormProvider
import models.{SchemeDetails, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import pages.event20A.Event20APspDeclarationPage
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event20A.Event20APspDeclarationView

import scala.concurrent.Future

class Event20APspDeclarationControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints
  private val authorisingPsaId = Some("A1234567")
  private val formProvider = new Event20APspDeclarationFormProvider()
  private val form = formProvider(authorisingPsaId)
  val schemeName = "schemeName"
  val pstr = "87219363YN"
  val taxYear = "2022"
  val practitionerName = "John Smith"
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockSchemeDetailsConnector = mock[SchemeDetailsConnector]
  private val mockEventReportingConnector = mock[EventReportingConnector]
  private val mockMinimalConnector = mock[MinimalConnector]
  private val mockSchemeDetails = SchemeDetails("schemeName", "87219363YN", "Open", authorisingPsaId)
  private def getRoute: String = routes.Event20APspDeclarationController.onPageLoad(waypoints).url
  private def postRoute: String = routes.Event20APspDeclarationController.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[SchemeDetailsConnector].toInstance(mockSchemeDetailsConnector),
    bind[EventReportingConnector].toInstance(mockEventReportingConnector),
    bind[MinimalConnector].toInstance(mockMinimalConnector)
  )

  private val validValue = "abc"

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
    reset(mockSchemeDetailsConnector)
    reset(mockEventReportingConnector)
    reset(mockMinimalConnector)
  }

  val testEmail = "test@test.com"
  val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()
  val mockMinimalDetails: MinimalDetails = {
    MinimalDetails(testEmail, false, None, Some(IndividualDetails(firstName = "John", None,  lastName = "Smith")), false, false)
  }

  "Event20APspDeclaration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()
      when(mockMinimalConnector.getMinimalDetails(any(), any())(any(), any())).thenReturn(Future.successful(mockMinimalDetails))

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[Event20APspDeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(schemeName, pstr, taxYear, practitionerName, form, waypoints)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockMinimalConnector.getMinimalDetails(any(), any())(any(), any())).thenReturn(Future.successful(mockMinimalDetails))
      val userAnswers = emptyUserAnswersWithTaxYear.set(Event20APspDeclarationPage, validValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[Event20APspDeclarationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          schemeName, pstr, taxYear, practitionerName, form.fill(validValue), waypoints)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockMinimalConnector.getMinimalDetails(any(), any())(any(), any())).thenReturn(Future.successful(mockMinimalDetails))
      when(mockSchemeDetailsConnector.getPspSchemeDetails(any(), any())(any(), any())).thenReturn(Future.successful(mockSchemeDetails))
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockEventReportingConnector.submitReportEvent20A(
        any(), any())(any(), any())).thenReturn(Future.successful(()))
      val application =
        applicationBuilder(userAnswers = Some(sampleEvent20ABecameJourneyData), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "A1234567"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswersWithTaxYear.set(Event20APspDeclarationPage, validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual Event20APspDeclarationPage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      when(mockMinimalConnector.getMinimalDetails(any(), any())(any(), any())).thenReturn(Future.successful(mockMinimalDetails))
      when(mockSchemeDetailsConnector.getPspSchemeDetails(any(), any())(any(), any())).thenReturn(Future.successful(mockSchemeDetails))
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockEventReportingConnector.submitReportEvent20A(
       any(), any())(any(), any())).thenReturn(Future.successful())
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ""))

        val view = application.injector.instanceOf[Event20APspDeclarationView]
        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(schemeName, pstr, taxYear, practitionerName, boundForm, waypoints)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}