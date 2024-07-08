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
import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import data.SampleData._
import forms.address.EnterPostcodeFormProvider
import models.enumeration.AddressJourneyType.Event1EmployerAddressJourney
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.address.EnterPostcodePage
import pages.event1.employer.CompanyDetailsPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.address.EnterPostcodeView

import scala.concurrent.Future

class EnterPostcodeControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new EnterPostcodeFormProvider()
  private val form = formProvider(companyDetails.companyName)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockAddressLookupConnector = mock[AddressLookupConnector]

  private def getRoute: String = routes.EnterPostcodeController.onPageLoad(waypoints, Event1EmployerAddressJourney, 0).url

  private def postRoute: String = routes.EnterPostcodeController.onSubmit(waypoints, Event1EmployerAddressJourney, 0).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
    reset(mockAddressLookupConnector)
    when(mockAddressLookupConnector.addressLookupByPostCode(any())(any(), any())).thenReturn(Future.successful(seqTolerantAddresses))
  }

  "EnterPostcode Controller" - {

    "must return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers.setOrException(CompanyDetailsPage(0), companyDetails)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[EnterPostcodeView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view.render(form, waypoints, Event1EmployerAddressJourney,
          messages("enterPostcode.title", "the company"),
          messages("enterPostcode.heading", companyDetails.companyName), index = 0, request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "zz11zz"))
        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.setOrException(EnterPostcodePage(Event1EmployerAddressJourney, 0), seqTolerantAddresses)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual EnterPostcodePage(Event1EmployerAddressJourney, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}
