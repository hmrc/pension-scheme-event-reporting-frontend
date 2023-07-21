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

package controllers

import base.SpecBase
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import forms.TaxYearFormProvider
import models.enumeration.JourneyStartType.{InProgress, PastEventTypes, StartNew}
import models.{EROverview, EROverviewVersion, TaxYear, ToggleDetails, UserAnswers}
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
import utils.DateHelper
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

  private val radioOptionsWithToggleOff: Seq[RadioItem] = TaxYear.options
  private val radioOptionsWithToggleOn: Seq[RadioItem] = TaxYear.optionsFiltered(taxYear => taxYear.startYear >= "2023")

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

  private val erOverview = Seq(overview1, overview2)
  private val mockEventConnector = mock[EventReportingConnector]

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockUserAnswersCacheConnector)
    DateHelper.setDate(Some(LocalDate.of(2023, 6, 1)))
  }

  "TaxYear Controller" - {

    "must return OK and the correct view for a GET with feature toggle OFF" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).bindings(
        bind[EventReportingConnector].to(mockEventConnector)).build()

      when(mockEventConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting-tax-year", None, isEnabled = false))
      )

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxYearView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, radioOptionsWithToggleOff)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET with feature toggle ON" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).bindings(
        bind[EventReportingConnector].to(mockEventConnector)).build()

      when(mockEventConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting-tax-year", None, isEnabled = true))
      )

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxYearView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, radioOptionsWithToggleOn)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when past events chosen from tile page" in {
      val ua = emptyUserAnswers
        .setOrException(EventReportingTileLinksPage, PastEventTypes)
        .setOrException(EventReportingOverviewPage, erOverview)

      val application = applicationBuilder(userAnswers = Some(ua)).bindings(
        bind[EventReportingConnector].to(mockEventConnector)).build()
      when(mockEventConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting-tax-year", None, isEnabled = false))
      )
      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val radioOptions: Seq[RadioItem] = TaxYear.optionsFiltered(_.startYear == "2022")
        val view = application.injector.instanceOf[TaxYearView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, radioOptions)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when in progress events chosen from tile page" in {
      val ua = emptyUserAnswers
        .setOrException(EventReportingTileLinksPage, InProgress)
        .setOrException(EventReportingOverviewPage, erOverview)

      val application = applicationBuilder(userAnswers = Some(ua)).bindings(
        bind[EventReportingConnector].to(mockEventConnector)).build()
      when(mockEventConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting-tax-year", None, isEnabled = false))
      )

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val radioOptions: Seq[RadioItem] = TaxYear.optionsFiltered(_.startYear == "2021")

        val view = application.injector.instanceOf[TaxYearView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, radioOptions)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered with feature toggle OFF" in {

      val userAnswers = UserAnswers().set(TaxYearPage, TaxYear("2022")).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).bindings(
        bind[EventReportingConnector].to(mockEventConnector)).build()

      when(mockEventConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting-tax-year", None, isEnabled = false))
      )

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[TaxYearView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TaxYear.values.head), waypoints, radioOptionsWithToggleOff)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered with feature toggle ON" in {

      val userAnswers = UserAnswers().set(TaxYearPage, TaxYear("2022")).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).bindings(
        bind[EventReportingConnector].to(mockEventConnector)).build()

      when(mockEventConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting-tax-year", None, isEnabled = true))
      )

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val view = application.injector.instanceOf[TaxYearView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TaxYear.values.head), waypoints, radioOptionsWithToggleOn)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val ua = emptyUserAnswers.setOrException(EventReportingTileLinksPage, StartNew)

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules).bindings(
          bind[EventReportingConnector].to(mockEventConnector)).build()

      when(mockEventConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting-tax-year", None, isEnabled = false))
      )

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

    "must return bad request when invalid data is submitted with feature toggle OFF" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).bindings(
          bind[EventReportingConnector].to(mockEventConnector)).build()

      when(mockEventConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting-tax-year", None, isEnabled = false))
      )

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[TaxYearView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, radioOptionsWithToggleOff)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }

    "must return bad request when invalid data is submitted with feature toggle ON" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules).bindings(
          bind[EventReportingConnector].to(mockEventConnector)).build()

      when(mockEventConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting-tax-year", None, isEnabled = true))
      )

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[TaxYearView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, radioOptionsWithToggleOn)(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never()).save(any(), any(), any())(any(), any())
      }
    }

  }
}
