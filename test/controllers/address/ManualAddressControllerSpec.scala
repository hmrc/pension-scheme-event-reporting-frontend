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
import forms.address.ManualAddressFormProvider
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.MockitoSugar.{mock, reset}
import org.scalatest.BeforeAndAfterEach
import pages.EmptyWaypoints
import pages.address.{ChooseAddressPage, ManualAddressPage}
import pages.event1.employer.CompanyDetailsPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.CountryOptions
import views.html.address.ManualAddressView

import scala.concurrent.Future

class ManualAddressControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new ManualAddressFormProvider(countryOptions)
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.ManualAddressController.onPageLoad(waypoints, Event1EmployerAddressJourney).url

  private def postRoute: String = routes.ManualAddressController.onSubmit(waypoints, Event1EmployerAddressJourney).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[CountryOptions].toInstance(countryOptions)
  )

  private val entityType = "the company"

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "ManualAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers.setOrException(CompanyDetailsPage, companyDetails)

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManualAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, Event1EmployerAddressJourney,
          Messages("address.title", entityType),
          Messages("address.heading", companyDetails.companyName), countryOptions.options)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ua = emptyUserAnswers.setOrException(CompanyDetailsPage, companyDetails)
        .setOrException(ManualAddressPage(Event1EmployerAddressJourney), seqAddresses.head)

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[ManualAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(seqAddresses.head), waypoints, Event1EmployerAddressJourney,
          Messages("address.title", entityType),
          Messages("address.heading", companyDetails.companyName), countryOptions.options)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val ua = emptyUserAnswers.setOrException(CompanyDetailsPage, companyDetails)

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(
            "addressLine1" -> seqTolerantAddresses.head.addressLine1.get,
            "addressLine2" -> seqTolerantAddresses.head.addressLine2.get,
            "addressLine3" -> seqTolerantAddresses.head.addressLine3.get,
            "addressLine4" -> seqTolerantAddresses.head.addressLine4.get,
            "postCode" -> seqTolerantAddresses.head.postcode.get,
            "country" -> seqTolerantAddresses.head.countryOpt.get
          )

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(ChooseAddressPage(Event1EmployerAddressJourney), seqAddresses.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ChooseAddressPage(Event1EmployerAddressJourney)
          .navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val ua = emptyUserAnswers.setOrException(CompanyDetailsPage, companyDetails)

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(
            "addressLine1" -> "",
            "addressLine2" -> seqTolerantAddresses.head.addressLine2.get,
            "addressLine3" -> seqTolerantAddresses.head.addressLine3.get,
            "addressLine4" -> seqTolerantAddresses.head.addressLine4.get,
            "postCode" -> seqTolerantAddresses.head.postcode.get,
            "country" -> seqTolerantAddresses.head.countryOpt.get
          )

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
