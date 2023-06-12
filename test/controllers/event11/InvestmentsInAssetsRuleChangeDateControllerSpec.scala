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

package controllers.event11

import base.SpecBase
import connectors.UserAnswersCacheConnector
import controllers.event11.InvestmentsInAssetsRuleChangeDateControllerSpec.event11Date
import forms.event11.InvestmentsInAssetsRuleChangeDateFormProvider
import models.UserAnswers
import models.event11.Event11Date
import pages.EmptyWaypoints
import pages.event11.{Event11CheckYourAnswersPage, InvestmentsInAssetsRuleChangeDatePage}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event11.InvestmentsInAssetsRuleChangeDateView

import java.time.LocalDate
import scala.concurrent.Future

class InvestmentsInAssetsRuleChangeDateControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints

  private val formProvider = new InvestmentsInAssetsRuleChangeDateFormProvider()
  private val stubMin: LocalDate = LocalDate.of(2022, 4, 6)
  private val stubMax: LocalDate = LocalDate.of(2023, 4, 5)
  private val form = formProvider(stubMin, stubMax)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.InvestmentsInAssetsRuleChangeDateController.onPageLoad(waypoints).url

  private def postRoute: String = routes.InvestmentsInAssetsRuleChangeDateController.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val validAnswer = Event11Date(LocalDate.of(2022, 7, 12))

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "InvestmentsInAssetsRuleChangeDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InvestmentsInAssetsRuleChangeDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswersWithTaxYear.set(InvestmentsInAssetsRuleChangeDatePage, validAnswer).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[InvestmentsInAssetsRuleChangeDateView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), waypoints)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(event11Date(validAnswer.date): _*)

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswersWithTaxYear.set(InvestmentsInAssetsRuleChangeDatePage, validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual Event11CheckYourAnswersPage().route(waypoints).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[InvestmentsInAssetsRuleChangeDateView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }
  }
}

object InvestmentsInAssetsRuleChangeDateControllerSpec {
  private val dateKey = "value"

  private def event11Date(
                           value: LocalDate
                         ): Seq[(String, String)] = {
    val day = Tuple2(dateKey + ".day", s"${value.getDayOfMonth}")
    val month = Tuple2(dateKey + ".month", s"${value.getMonthValue}")
    val year = Tuple2(dateKey + ".year", s"${value.getYear}")
    Seq(day, month, year)
  }
}
