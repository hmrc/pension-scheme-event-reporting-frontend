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

package controllers.address

import base.SpecBase
import connectors.UserAnswersCacheConnector
import data.SampleData._
import forms.address.ChooseAddressFormProvider
import models.UserAnswers
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.address.{ChooseAddressPage, EnterPostcodePage, ManualAddressPage}
import pages.event1.employer.CompanyDetailsPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.address.ChooseAddressView

import scala.concurrent.Future

class ChooseAddressControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val formProvider = new ChooseAddressFormProvider()
  private val form = formProvider(seqAddresses)
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private def getRoute: String = routes.ChooseAddressController.onPageLoad(waypoints, Event1EmployerAddressJourney, 0).url

  private def postRoute: String = routes.ChooseAddressController.onSubmit(waypoints, Event1EmployerAddressJourney, 0).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "Choose address controller" - {
    "must return OK and the correct view for a GET" in {
      val ua = emptyUserAnswers
        .setOrException(EnterPostcodePage(Event1EmployerAddressJourney, 0), seqTolerantAddresses)
        .setOrException(CompanyDetailsPage(0), companyDetails)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChooseAddressView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual
          view.render(form, waypoints, Event1EmployerAddressJourney,
            messages("chooseAddress.title", "the company"),
            messages("chooseAddress.heading", companyDetails.companyName),
            seqTolerantAddresses, index = 0, request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockUserAnswersCacheConnector.save(any(), any(), uaCaptor.capture())(any(), any(), any())).thenReturn(Future.successful(()))

      val ua = emptyUserAnswers.setOrException(EnterPostcodePage(Event1EmployerAddressJourney, 0), seqTolerantAddresses)

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "0"))
        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.setOrException(ManualAddressPage(Event1EmployerAddressJourney, 0), seqAddresses.head)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ChooseAddressPage(Event1EmployerAddressJourney, 0).navigate(waypoints, updatedAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any(), any())
        uaCaptor.getValue.get(ManualAddressPage(Event1EmployerAddressJourney, 0)) mustBe Some(seqAddresses.head)
      }
    }

    "must return bad request when invalid data is submitted" in {
      val ua = emptyUserAnswers.setOrException(EnterPostcodePage(Event1EmployerAddressJourney, 0), seqTolerantAddresses)

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }

    "must return to the same page with errors if the form is invalid" in {
      val ua = emptyUserAnswers.setOrException(EnterPostcodePage(Event1EmployerAddressJourney, 0), seqTolerantAddresses)

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(messages("chooseAddress.error.required"))
      }
    }

    "must return bad request when no address is selected" in {
      val ua = emptyUserAnswers.setOrException(EnterPostcodePage(Event1EmployerAddressJourney, 0), seqTolerantAddresses)

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(messages("chooseAddress.error.required"))
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any(), any())
      }
    }
  }
}
