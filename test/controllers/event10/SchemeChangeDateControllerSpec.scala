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

package controllers.event10

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.event10.SchemeChangeDateFormProvider
import models.event10.{BecomeOrCeaseScheme, SchemeChangeDate}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.event10.{BecomeOrCeaseSchemePage, SchemeChangeDatePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event10.SchemeChangeDateView

import java.time.LocalDate
import scala.concurrent.Future

class SchemeChangeDateControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val stubMin: LocalDate = LocalDate.of(2022, 4, 6) //06-04-2006
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5) //05-04-2023

  private val formProvider = new SchemeChangeDateFormProvider()
  private val form = formProvider(stubMin, stubMax)
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val becomeAScheme: String = "itBecameAnInvestmentRegulatedPensionScheme"
  private val ceasedToBecomeAScheme: String = "itHasCeasedToBeAnInvestmentRegulatedPensionScheme"

  private def getRoute: String = routes.SchemeChangeDateController.onPageLoad(waypoints).url

  private def postRoute: String = routes.SchemeChangeDateController.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validValue = SchemeChangeDate(LocalDate.of(2022, 7, 12))

  import SchemeChangeDateControllerSpec._

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "SchemeChangeDate Controller" - {

    "must return OK and the correct view for a GET (Became a scheme)" in {

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItBecameAnInvestmentRegulatedPensionScheme)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SchemeChangeDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, becomeAScheme)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET (Ceased to become a scheme)" in {

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItHasCeasedToBeAnInvestmentRegulatedPensionScheme)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SchemeChangeDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, ceasedToBecomeAScheme)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered (Become a scheme)" in {

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItBecameAnInvestmentRegulatedPensionScheme)
        .set(SchemeChangeDatePage, validValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[SchemeChangeDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validValue), waypoints, becomeAScheme)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered (Ceased to become a scheme)" in {

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItHasCeasedToBeAnInvestmentRegulatedPensionScheme)
        .set(SchemeChangeDatePage, validValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[SchemeChangeDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validValue), waypoints, ceasedToBecomeAScheme)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted (Become a scheme)" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItBecameAnInvestmentRegulatedPensionScheme)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(schemeChangeDate(validValue.schemeChangeDate): _*)

        val result = route(application, request).value
        val updatedAnswers = userAnswers.set(SchemeChangeDatePage, validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual SchemeChangeDatePage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted (Ceased to become a scheme)" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItHasCeasedToBeAnInvestmentRegulatedPensionScheme)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(schemeChangeDate(validValue.schemeChangeDate): _*)

        val result = route(application, request).value
        val updatedAnswers = userAnswers.set(SchemeChangeDatePage, validValue).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual SchemeChangeDatePage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted (Became a scheme)" in {

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItBecameAnInvestmentRegulatedPensionScheme)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[SchemeChangeDateView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, becomeAScheme)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted (Ceased to become a scheme)" in {

      val userAnswers = emptyUserAnswersWithTaxYear.setOrException(BecomeOrCeaseSchemePage, BecomeOrCeaseScheme.ItHasCeasedToBeAnInvestmentRegulatedPensionScheme)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[SchemeChangeDateView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, ceasedToBecomeAScheme)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}

object SchemeChangeDateControllerSpec {
  private val schemeChangeDateKey = "schemeChangeDate"

  private def schemeChangeDate(
                                schemeChangeDate: LocalDate
                              ): Seq[(String, String)] = {
    val day = Tuple2(schemeChangeDateKey + ".day", s"${schemeChangeDate.getDayOfMonth}")
    val month = Tuple2(schemeChangeDateKey + ".month", s"${schemeChangeDate.getMonthValue}")
    val year = Tuple2(schemeChangeDateKey + ".year", s"${schemeChangeDate.getYear}")
    Seq(day, month, year)
  }

}
