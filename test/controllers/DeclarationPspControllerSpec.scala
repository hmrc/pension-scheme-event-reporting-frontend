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

package controllers

import base.SpecBase
import connectors.MinimalConnector.{IndividualDetails, MinimalDetails}
import connectors.{EventReportingConnector, MinimalConnector, SchemeConnector, UserAnswersCacheConnector}
import data.SampleData.sampleEvent20JourneyData
import forms.DeclarationPspFormProvider
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.{AuthorisingPSA, PspDetails, PspSchemeDetails, UserAnswers, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{DeclarationPspPage, EmptyWaypoints, VersionInfoPage}
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Results.{BadRequest, Ok}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, _}
import services.SubmitService
import views.html.DeclarationPspView

import java.time.LocalDate
import scala.concurrent.Future

class DeclarationPspControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints
  private val authorisingPsaId = Some("A1234567")
  private val formProvider = new DeclarationPspFormProvider()
  private val form = formProvider(authorisingPsaId)
  private val practitionerName = "John Smith"


  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockSchemeDetailsConnector = mock[SchemeConnector]
  private val mockEventReportingConnector = mock[EventReportingConnector]
  private val mockSubmitService = mock[SubmitService]
  private val mockMinimalConnector = mock[MinimalConnector]
  private val mockSchemeDetails = PspSchemeDetails("schemeName", "87219363YN", "Open", Some(PspDetails(None, None, None, authorisingPsaId.get, AuthorisingPSA(None, None, None, None), LocalDate.now(), "")))
  val testEmail = "test@test.com"
  val mockMinimalDetails: MinimalDetails = {
    MinimalDetails(
      testEmail,
      isPsaSuspended = false,
      None,
      Some(IndividualDetails(firstName = "John", None, lastName = "Smith")),
      rlsFlag = false, deceasedFlag = false
    )
  }

  private def getRoute: String = routes.DeclarationPspController.onPageLoad(waypoints).url

  private def postRoute: String = routes.DeclarationPspController.onSubmit(waypoints).url

  private val validValue = "abc"

  override def beforeEach(): Unit = {
    reset(mockUserAnswersCacheConnector)
    reset(mockSchemeDetailsConnector)
    reset(mockSubmitService)
    reset(mockMinimalConnector)
  }

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[SchemeConnector].toInstance(mockSchemeDetailsConnector),
    bind[EventReportingConnector].toInstance(mockEventReportingConnector),
    bind[SubmitService].toInstance(mockSubmitService),
    bind[MinimalConnector].toInstance(mockMinimalConnector)
  )

  "DeclarationPsp Controller" - {

    "must return OK and the correct view for a GET when when isReportSubmitted is false" in {
      val userAnswersWithVersionInfo = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()
      when(mockMinimalConnector.getMinimalDetails(any())(any(), any())).thenReturn(Future.successful(mockMinimalDetails))

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationPspView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(practitionerName, form, waypoints)(request, messages(application)).toString
      }
    }

    "must redirect to cannot resume page when isReportSubmitted is true" in {
      val userAnswersWithVersionInfo = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Submitted))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()
      when(mockMinimalConnector.getMinimalDetails(any())(any(), any())).thenReturn(Future.successful(mockMinimalDetails))

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CannotResumeController.onPageLoad(waypoints).url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      when(mockMinimalConnector.getMinimalDetails(any())(any(), any())).thenReturn(Future.successful(mockMinimalDetails))
      val userAnswers = UserAnswers().set(DeclarationPspPage, validValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[DeclarationPspView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(practitionerName, form.fill(validValue), waypoints)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockMinimalConnector.getMinimalDetails(any())(any(), any())).thenReturn(Future.successful(mockMinimalDetails))
      when(mockSchemeDetailsConnector.getPspSchemeDetails(any(), any(), any())(any(), any())).thenReturn(Future.successful(mockSchemeDetails))
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))
      when(mockSubmitService.submitReport(any(), any())(any(), any(), any())).thenReturn(Future.successful(Ok))

      val application =
        applicationBuilder(userAnswers = Some(sampleEvent20JourneyData), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "A1234567"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSubmittedController.onPageLoad(waypoints).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      when(mockMinimalConnector.getMinimalDetails(any())(any(), any())).thenReturn(Future.successful(mockMinimalDetails))
      when(mockSchemeDetailsConnector.getPspSchemeDetails(any(), any(), any())(any(), any())).thenReturn(Future.successful(mockSchemeDetails))
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ""))

        val view = application.injector.instanceOf[DeclarationPspView]
        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces() mustEqual view(practitionerName, boundForm, waypoints)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must redirect to the correct error screen when no data is able to be submitted" in {
      val applicationNoUA = applicationBuilder(userAnswers = None, extraModules).build()

      running(applicationNoUA) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "A1234567"))

        val result = route(applicationNoUA, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockUserAnswersCacheConnector, times(0)).save(any(), any())(any(), any(), any())
        verify(mockSubmitService, times(0)).submitReport(any(), any())(any(), any(), any())
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the cannot resume page for method onClick when report has been submitted multiple times in quick succession" in {
      when(mockMinimalConnector.getMinimalDetails(any())(any(), any())).thenReturn(Future.successful(mockMinimalDetails))
      when(mockSchemeDetailsConnector.getPspSchemeDetails(any(), any(), any())(any(), any())).thenReturn(Future.successful(mockSchemeDetails))
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))
      when(mockSubmitService.submitReport(any(), any())(any(), any(), any())).thenReturn(Future.successful(BadRequest))

      val application =
        applicationBuilder(userAnswers = Some(sampleEvent20JourneyData), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "A1234567"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any(), any())
        verify(mockSubmitService, times(1)).submitReport(any(), any())(any(), any(), any())
        redirectLocation(result).value mustEqual routes.CannotResumeController.onPageLoad(waypoints).url
      }
    }
  }
}
