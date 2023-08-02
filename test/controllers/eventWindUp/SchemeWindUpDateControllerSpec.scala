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

package controllers.eventWindUp

import base.SpecBase
import connectors.{SchemeConnector, UserAnswersCacheConnector}
import forms.eventWindUp.SchemeWindUpDateFormProvider
import helpers.DateHelper
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.eventWindUp.SchemeWindUpDatePage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.eventWindUp.SchemeWindUpDateView

import java.time.LocalDate
import scala.concurrent.Future

class SchemeWindUpDateControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {
//TODO finish tests for openDate

  private val waypoints = EmptyWaypoints
  private val openDate = LocalDate.of(2022, 5, 1)
  private val formProvider = new SchemeWindUpDateFormProvider
  private val form = formProvider(2022, openDate)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockSchemeConnector = mock[SchemeConnector]
  private val mockTaxYear = mock[DateHelper]


  private def getRoute: String = routes.SchemeWindUpDateController.onPageLoad(waypoints).url

  private def postRoute: String = routes.SchemeWindUpDateController.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[SchemeConnector].toInstance(mockSchemeConnector),
    bind[DateHelper].toInstance(mockTaxYear)
  )

  private val validAnswer = LocalDate.of(2022, 5, 12)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
    reset(mockSchemeConnector)
    when(mockTaxYear.now).thenReturn(validAnswer)
  }

  "SchemeWindUpDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SchemeWindUpDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswersWithTaxYear.set(SchemeWindUpDatePage, validAnswer).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[SchemeWindUpDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), waypoints)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockUserAnswersCacheConnector.save(any(), any(), uaCaptor.capture())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockSchemeConnector.getOpenDate("psaid", "0000", "pstr")(any(), any()))
        .thenReturn(Future.successful(openDate))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(
            "value.day" -> "12",
            "value.month" -> "5",
            "value.year" -> "2022"
          )

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(SchemeWindUpDatePage, validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual SchemeWindUpDatePage.navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
        uaCaptor.getValue.get(SchemeWindUpDatePage) mustBe Some(validAnswer)
      }
    }

    "must return bad request when invalid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockSchemeConnector.getOpenDate("psaid", "0000", "pstr")(any(), any()))
        .thenReturn(Future.successful(openDate))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[SchemeWindUpDateView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
      }
    }
  }
}
