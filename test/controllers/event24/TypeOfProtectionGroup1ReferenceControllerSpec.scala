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

package controllers.event24

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event24.TypeOfProtectionGroup1ReferenceFormProvider
import models.UserAnswers
import models.event24.{ProtectionReferenceData, TypeOfProtectionGroup1}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import pages.event24.{TypeOfProtectionGroup1Page, TypeOfProtectionGroup1ReferencePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event24.TypeOfProtectionGroup1ReferenceView

import scala.concurrent.Future

class TypeOfProtectionGroup1ReferenceControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new TypeOfProtectionGroup1ReferenceFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.TypeOfProtectionGroup1ReferenceController.onPageLoad(waypoints, 0).url
  private def postRoute: String = routes.TypeOfProtectionGroup1ReferenceController.onSubmit(waypoints, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validValue = ProtectionReferenceData("abc123DEF", "", "", "")

  private val protectionTypesAnswer: Set[TypeOfProtectionGroup1] = Set(
    TypeOfProtectionGroup1.RecognisedOverseasPSTE
  )

  val ua: UserAnswers = emptyUserAnswers.setOrException(TypeOfProtectionGroup1Page(0), protectionTypesAnswer)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "TypeOfProtectionReferenceController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TypeOfProtectionGroup1ReferenceView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, 0, protectionTypesAnswer.toSeq)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua.set(TypeOfProtectionGroup1ReferencePage(0), validValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[TypeOfProtectionGroup1ReferenceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form.fill(validValue), waypoints, 0, protectionTypesAnswer.toSeq)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("recognisedOverseasPSTE", "abc123DEF"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(TypeOfProtectionGroup1ReferencePage(0), validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TypeOfProtectionGroup1ReferencePage(0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("recognisedOverseasPSTE", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }
  }
}
