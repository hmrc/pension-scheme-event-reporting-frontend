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

package controllers.event6

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event6.InputProtectionTypeFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.event6.InputProtectionTypePage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event6.InputProtectionTypeView

import scala.concurrent.Future

class InputProtectionTypeControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val formProvider = new InputProtectionTypeFormProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.InputProtectionTypeController.onPageLoad(waypoints).url
  private def postRoute: String = routes.InputProtectionTypeController.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }


  "InputProtectionType Controller" - {
    testInputProtectionTypeReference(formProvider("enhancedLifetimeAllowance"),
      "enhancedLifetimeAllowance", "ABC123654XYZ", "ABC123654YZ", "ABC123654Y")

    //    testInputProtectionTypeReference(formProvider("enhancedProtection"),
    //      "enhancedProtection", "1234567A", "1234567A", "1234567")

    //    testInputProtectionTypeReference(formProvider("fixedProtection"),
    //      "fixedProtection", "8111111A", "8111111A",  "8111111")

    //    testInputProtectionTypeReference(formProvider("fixedProtection2014"),
    //      "fixedProtection2014", "IP149999999999X", "IP149999999999X", "IP149999999999")
    //    testInputProtectionTypeReference(formProvider("fixedProtection2014"),
    //      "fixedProtection2014", "1234567A", "1234567A", "1234567")

    //    testInputProtectionTypeReference(formProvider("fixedProtection2016"),
    //      "fixedProtection2016", "FP1600000000000", "FP1600000000000", "FP160000000000")

    //    testInputProtectionTypeReference(formProvider("individualProtection2014"),
    //      "individualProtection2014", "IP149999999999X", "IP149999999999X", "IP149999999999")
    //    testInputProtectionTypeReference(formProvider("individualProtection2014"),
    //      "individualProtection2014", "A999999A", "A999999A", "A999999")
    //
    //    testInputProtectionTypeReference(formProvider("individualProtection2016"),
    //      "individualProtection2016", "IP1622222222222", "IP1622222222222", "IP162222222222")
  }

  private def testInputProtectionTypeReference(form: Form[String], protectionType: String, requestValue: String,
                                               validValue: String, invalidValue: String): Unit = {
    correctViewTest(form, protectionType, validValue)
    correctViewWithExistingDataTest(form, protectionType, validValue)
    validDataTest(protectionType, requestValue, validValue)
    invalidDataTest(form, protectionType, invalidValue)
  }

  private def correctViewTest(form: Form[String], protectionType: String, requestValue: String): Unit = {
    s"must return OK and the correct view for a GET for $protectionType reference and $requestValue " in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InputProtectionTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, protectionType)(request, messages(application)).toString
      }
    }
  }

  private def correctViewWithExistingDataTest(form: Form[String], protectionType: String, validValue: String): Unit = {
    s"must populate the view correctly on a GET when the question has previously been answered for $protectionType reference and $validValue" in {

      val userAnswers = UserAnswers().set(InputProtectionTypePage, validValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[InputProtectionTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validValue), waypoints, protectionType)(request, messages(application)).toString
      }
    }
  }

  private def validDataTest(protectionType: String, requestValue: String, validValue: String): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for $protectionType reference and $validValue" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value",requestValue))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(InputProtectionTypePage, validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual InputProtectionTypePage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }
  }

  private def invalidDataTest(form: Form[String], protectionType: String, invalidValue: String): Unit = {
    s"must return bad request when invalid data is submitted for $protectionType reference with $invalidValue" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", invalidValue))

        val view = application.injector.instanceOf[InputProtectionTypeView]
        val boundForm = form.bind(Map("value" -> invalidValue))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, protectionType)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
