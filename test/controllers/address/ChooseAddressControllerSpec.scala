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
import models.UserAnswers
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mockito.MockitoSugar.{mock, reset}
import org.scalatest.BeforeAndAfterEach
import pages.EmptyWaypoints
import pages.address.{ChooseAddressPage, EnterPostcodePage, ManualAddressPage}
import pages.event1.employer.CompanyDetailsPage
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

  "Choose address controller" - {

    "must return OK and the correct view for a GET" in {
      val ua = emptyUserAnswers
        .setOrException(EnterPostcodePage(Event1EmployerAddressJourney), seqTolerantAddresses)
        .setOrException(CompanyDetailsPage, companyDetails)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChooseAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, waypoints, Event1EmployerAddressJourney,
            messages("chooseAddress.title", "the company"),
            messages("chooseAddress.heading", companyDetails.companyName),
            seqTolerantAddresses)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockUserAnswersCacheConnector.save(any(), any(), uaCaptor.capture())(any(), any()))
        .thenReturn(Future.successful(()))

      val ua = emptyUserAnswers.setOrException(EnterPostcodePage(Event1EmployerAddressJourney), seqTolerantAddresses)

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "0"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(ChooseAddressPage(Event1EmployerAddressJourney), seqAddresses.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ChooseAddressPage(Event1EmployerAddressJourney)
          .navigate(waypoints, ua.setOrException(ChooseAddressPage(Event1EmployerAddressJourney), seqAddresses.head), updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
        uaCaptor.getValue.get(ManualAddressPage(Event1EmployerAddressJourney)) mustBe Some(seqAddresses.head)
      }
    }

    "must return bad request when invalid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val ua = emptyUserAnswers.setOrException(EnterPostcodePage(Event1EmployerAddressJourney), seqTolerantAddresses)

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
