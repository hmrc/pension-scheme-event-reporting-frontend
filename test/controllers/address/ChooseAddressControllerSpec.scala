/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.address

import base.SpecBase
import connectors.UserAnswersCacheConnector
import data.SampleData._
import forms.address.ChooseAddressFormProvider
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{mock, reset}
import org.scalatest.BeforeAndAfterEach
import pages.EmptyWaypoints
import pages.address.{ChooseAddressPage, EnterPostcodePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.address.ChooseAddressView

import scala.concurrent.Future

class ChooseAddressControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new ChooseAddressFormProvider()
  private val form = formProvider(seqAddresses)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.ChooseAddressController.onPageLoad(waypoints, Event1EmployerAddressJourney).url
  private def postRoute: String = routes.ChooseAddressController.onSubmit(waypoints, Event1EmployerAddressJourney).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "Test Controller" - {

    "must return OK and the correct view for a GET" in {
      val ua = emptyUserAnswers
        .setOrException(EnterPostcodePage(Event1EmployerAddressJourney), seqTolerantAddresses)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChooseAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, waypoints, Event1EmployerAddressJourney, "", "", seqTolerantAddresses)(request, messages(application)).toString
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
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "0"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(ChooseAddressPage(Event1EmployerAddressJourney), seqAddresses.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ChooseAddressPage(Event1EmployerAddressJourney)
          .navigate(waypoints, emptyUserAnswers, updatedAnswers).url
      }
    }

    "must return bad request when invalid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[ChooseAddressView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(form, waypoints, Event1EmployerAddressJourney, "", "", seqTolerantAddresses)(request, messages(application)).toString
      }
    }
  }
}
