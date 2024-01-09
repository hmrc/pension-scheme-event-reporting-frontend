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

import audit.{AuditService, StartNewERAuditEvent}
import base.SpecBase
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import forms.EventSelectionFormProvider
import models.EventSelection.Event24
import models.enumeration.{EventType, VersionStatus}
import models.{EventSelection, TaxYear, ToggleDetails, VersionInfo}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually.eventually
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, EventSelectionPage, TaxYearPage, VersionInfoPage}
import play.api.inject
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.EventSelectionView

import scala.concurrent.Future

class EventSelectionControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach with MockitoSugar {
  private val waypoints = EmptyWaypoints

  private def postRoute: String = routes.EventSelectionController.onSubmit(waypoints).url

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockEventConnector = mock[EventReportingConnector]
  private val mockAuditService = mock[AuditService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    inject.bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    inject.bind[EventReportingConnector].toInstance(mockEventConnector),
    inject.bind[AuditService].toInstance(mockAuditService)
  )

  private val psaId = "psaId"
  private val pstr = "87219363YN"
  private val eventType = EventType.Event1
  private val reportVersion = "1"

  override protected def beforeEach(): Unit = {
    reset(mockUserAnswersCacheConnector)
    reset(mockEventConnector)
    reset(mockAuditService)
  }

  "GET" - {
    "must return OK and the correct view for date prior to 2024" in {
      val ua = emptyUserAnswers
        .setOrException(TaxYearPage, TaxYear("2022"))
      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()
      when(mockEventConnector.getFeatureToggle(any())(any())).thenReturn(
        Future.successful(ToggleDetails("lta-events-show-hide", None, isEnabled = false))
      )
      val view = application.injector.instanceOf[EventSelectionView]

      running(application) {
        val request = FakeRequest(GET, routes.EventSelectionController.onPageLoad().url)
        val result = route(application, request).value

        val formProvider = new EventSelectionFormProvider()
        val form = formProvider()

        val expectedEvents = EventSelection.values.diff(Seq(Event24))
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, EventSelection.options(expectedEvents), waypoints)(request, messages(application)).toString
      }
    }
  }

  "POST" - {
    "must redirect to next page on submit (when selecting an option) and send audit event" in {

      val ua = emptyUserAnswersWithTaxYear.setOrException(TaxYearPage, TaxYear("2022"))
        .setOrException(VersionInfoPage, VersionInfo(1, VersionStatus.Submitted))
      val application = applicationBuilder(Some(ua), extraModules).build()

      running(application) {
        when(mockEventConnector.getFeatureToggle(any())(any())).thenReturn(
          Future.successful(ToggleDetails("lta-events-show-hide", None, isEnabled = false))
        )
        when(mockUserAnswersCacheConnector.get(any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(ua)))
        doNothing().when(mockAuditService).sendEvent(any())(any(), any())

        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "event1"))

        val result = route(application, request).value
        val userAnswerUpdated = emptyUserAnswers
          .setOrException(TaxYearPage, TaxYear("2022"))
          .setOrException(EventSelectionPage, EventSelection.Event1)

        val taxYear = TaxYear.getSelectedTaxYear(userAnswerUpdated)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual EventSelectionPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).url

        eventually {
          val expectedAuditEvent = StartNewERAuditEvent(psaId, pstr, taxYear, eventType, reportVersion)
          verify(mockAuditService, times(1)).sendEvent(ArgumentMatchers.eq(expectedAuditEvent))(any(), any())
        }
      }
    }
  }
}
