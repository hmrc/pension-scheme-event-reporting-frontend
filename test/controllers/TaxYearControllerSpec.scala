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

package controllers

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.TaxYearFormProvider
import models.enumeration.JourneyStartType.{InProgress, PastEventTypes, StartNew}
import models.{EROverview, EROverviewVersion, TaxYear, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, EventReportingOverviewPage, EventReportingTileLinksPage, TaxYearPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.TaxYearView

import java.time.LocalDate
import scala.concurrent.Future

class TaxYearControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new TaxYearFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.TaxYearController.onPageLoad(waypoints).url

  private def postRoute: String = routes.TaxYearController.onSubmit(waypoints).url

  private val radioOptions: Seq[RadioItem] = TaxYear.optionsFiltered(taxYear => taxYear.startYear >= "2023")

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val overview1 = EROverview(
    LocalDate.of(2021, 4, 6),
    LocalDate.of(2022, 4, 5),
    TaxYear("2021"),
    tpssReportPresent = false,
    Some(EROverviewVersion(
      3,
      submittedVersionAvailable = false,
      compiledVersionAvailable = true)))

  private val overview2 = EROverview(
    LocalDate.of(2022, 4, 6),
    LocalDate.of(2023, 4, 5),
    TaxYear("2022"),
    tpssReportPresent = false,
    Some(EROverviewVersion(
      2,
      submittedVersionAvailable = true,
      compiledVersionAvailable = false)))

  private val overview3 =  EROverview(
    LocalDate.of(2023, 4, 6),
    LocalDate.of(2024, 4, 5),
    TaxYear("2023"),
    tpssReportPresent = false,
    Some(EROverviewVersion(
      3,
      submittedVersionAvailable = false,
      compiledVersionAvailable = false)))

  private val overview4 =  EROverview(
    LocalDate.of(2024, 4, 6),
    LocalDate.of(2025, 4, 5),
    TaxYear("2024"),
    tpssReportPresent = false,
    Some(EROverviewVersion(
      3,
      submittedVersionAvailable = false,
      compiledVersionAvailable = false)))
  private val erOverview = Seq(overview1, overview2)

  private val eventReportingUrl = "/manage-pension-scheme-event-report/87219363YN/event-reporting"
  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
  }

  "TaxYear Controller" - {

    "must return OK and the correct view for a GET with feature toggle OFF" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxYearView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, radioOptions, eventReportingUrl)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when past events chosen from tile page" in {
      val ua = emptyUserAnswers
        .setOrException(EventReportingTileLinksPage, PastEventTypes)
        .setOrException(EventReportingOverviewPage, erOverview)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val radioOptions: Seq[RadioItem] = TaxYear.optionsFiltered(_.startYear == "2022")
        val view = application.injector.instanceOf[TaxYearView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, radioOptions, eventReportingUrl)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when in progress events chosen from tile page" in {
      val ua = emptyUserAnswers
        .setOrException(EventReportingTileLinksPage, InProgress)
        .setOrException(EventReportingOverviewPage, erOverview)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val radioOptions: Seq[RadioItem] = TaxYear.optionsFiltered(_.startYear == "2021")

        val view = application.injector.instanceOf[TaxYearView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, radioOptions, eventReportingUrl)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when a start new event chosen from tile page" in {

      val ua = emptyUserAnswers
        .setOrException(EventReportingTileLinksPage, StartNew)
        .setOrException(EventReportingOverviewPage, Seq(overview3, overview4) ++ erOverview)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val radioOptions: Seq[RadioItem] = TaxYear.optionsFiltered(taxYear => taxYear.startYear == "2024" ||taxYear.startYear == "2023" )

        val view = application.injector.instanceOf[TaxYearView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, radioOptions, eventReportingUrl)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when a start new event chosen and no events left" in {

      val submittedVersion = Some(EROverviewVersion(
        3,
        submittedVersionAvailable = true,
        compiledVersionAvailable = false))

      val ua = emptyUserAnswers
        .setOrException(EventReportingTileLinksPage, StartNew)
        .setOrException(EventReportingOverviewPage, Seq(overview3.copy(versionDetails = submittedVersion), overview4.copy(versionDetails = submittedVersion) ) ++ erOverview)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxYearView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, Seq.empty[RadioItem], eventReportingUrl)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers().set(TaxYearPage, TaxYear("2022")).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[TaxYearView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form.fill(TaxYear.values.head), waypoints, radioOptions, eventReportingUrl)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val ua = emptyUserAnswers.setOrException(EventReportingTileLinksPage, StartNew)

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules).build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", TaxYear.values.head.startYear))

        val result = route(application, request).value
        val updatedAnswers = ua.set(TaxYearPage, TaxYear.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TaxYearPage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        verify(mockUserAnswersCacheConnector, times(1)).save(any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[TaxYearView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces() mustEqual view(boundForm, waypoints, radioOptions, eventReportingUrl)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }

  }
}
