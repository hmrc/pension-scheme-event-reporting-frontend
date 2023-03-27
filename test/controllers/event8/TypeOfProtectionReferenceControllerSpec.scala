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

package controllers.event8

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event8.TypeOfProtectionReferenceFormProvider
import models.UserAnswers
import models.enumeration.EventType
import models.event8.TypeOfProtection
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.event8.{TypeOfProtectionPage, TypeOfProtectionReferencePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event8.TypeOfProtectionReferenceView

import scala.concurrent.Future

class TypeOfProtectionReferenceControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val eventType = EventType.Event8

  private val formProvider = new TypeOfProtectionReferenceFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val primaryProtection: UserAnswers = UserAnswers().setOrException(TypeOfProtectionPage(EventType.Event8, 0), TypeOfProtection.PrimaryProtection)
  private val enhancedProtection: UserAnswers = UserAnswers().setOrException(TypeOfProtectionPage(EventType.Event8, 0), TypeOfProtection.EnhancedProtection)

  private def getRoute: String = routes.TypeOfProtectionReferenceController.onPageLoad(waypoints, 0).url

  private def postRoute: String = routes.TypeOfProtectionReferenceController.onSubmit(waypoints, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "TypeOfProtectionReference Controller" - {
    testTypeOfProtectionReference(primaryProtection, TypeOfProtection.PrimaryProtection.toString)
    testTypeOfProtectionReference(enhancedProtection, TypeOfProtection.EnhancedProtection.toString)
  }

  private def testTypeOfProtectionReference(userAnswers: UserAnswers, protectionType: String): Unit = {
    correctViewTest(userAnswers, protectionType)
    correctViewWithExistingDataTest(userAnswers, protectionType)
    validDataTest(protectionType)
    invalidDataTest(userAnswers, protectionType)
  }

  private def correctViewTest(userAnswers: UserAnswers, typeOfProtection: String): Unit = {
    s"must return OK and the correct view for a GET for $typeOfProtection reference" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val protectionType = messages(s"event8.typeOfProtection.$typeOfProtection").toLowerCase
      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TypeOfProtectionReferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, 0, protectionType)(request, messages(application)).toString
      }
    }
  }

  private def correctViewWithExistingDataTest(userAnswers: UserAnswers, typeOfProtection: String): Unit = {
    s"must populate the view correctly on a GET when the question has previously been answered for $typeOfProtection reference" in {
      val updatedAnswers = userAnswers.setOrException(TypeOfProtectionReferencePage(eventType, 0), "ABCDE123")
      val application = applicationBuilder(userAnswers = Some(updatedAnswers)).build()
      val protectionTypeDesc = messages(s"event8.typeOfProtection.$typeOfProtection").toLowerCase

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[TypeOfProtectionReferenceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("ABCDE123"), waypoints, 0, protectionTypeDesc)(request, messages(application)).toString
      }
    }
  }

  private def validDataTest(typeOfProtection: String): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for $typeOfProtection reference" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "ABCDE123"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(TypeOfProtectionReferencePage(eventType = eventType, index = 0), "ABCDE123").success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TypeOfProtectionReferencePage(eventType, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }
  }

  private def invalidDataTest(userAnswers: UserAnswers, typeOfProtection: String): Unit = {
    s"must return bad request when invalid data is submitted for $typeOfProtection reference" in {
      val application =
        applicationBuilder(userAnswers = Some(userAnswers), extraModules)
          .build()
      val protectionTypeDesc = messages(s"event8.typeOfProtection.$typeOfProtection").toLowerCase

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid-data"))

        val view = application.injector.instanceOf[TypeOfProtectionReferenceView]
        val boundForm = form.bind(Map("value" -> "invalid-data"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, 0, protectionTypeDesc)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
