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
import config.FrontendAppConfig
import connectors.{AFTFrontendConnector, EventReportingConnector, UserAnswersCacheConnector}
import forms.TaxYearFormProvider
import models.enumeration.JourneyStartType.StartNew
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, EventReportingTileLinksPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.OverviewViewModel
import views.html.EventReportingOverviewView

import scala.concurrent.{ExecutionContext, Future}


class EventReportingOverviewControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val waypoints = EmptyWaypoints
  val ua = emptyUserAnswers.setOrException(EventReportingTileLinksPage, StartNew)
  val ovm = OverviewViewModel(pastYears = Seq(("", "")), yearsInProgress = Seq(("", "")), schemeName = "schemeName",
    outstandingAmount = "£19.00",
    paymentsAndChargesUrl = "/manage-pension-scheme-accounting-for-tax/S2400000041/financial-overview/event-reporting/select-charges-year",
      newEventReportingUrl = "/manage-pension-scheme-event-report/event-reporting-date?taxYear=&journeyType=StartNew")

  private val formProvider = new TaxYearFormProvider()
  private val form = formProvider()
  private val amountHtml = Html("£19.00")
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockEventReportingConnector = mock[EventReportingConnector]
  private val mockAFTFrontendConnector = mock[AFTFrontendConnector]
  private val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  private val dummyUrl = "dummy"
  private def getRoute: String = routes.EventReportingOverviewController.onPageLoad("S2400000041").url

  private def postRoute: String = routes.EventReportingOverviewController.onSubmit("2022", "InProgress").url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[EventReportingConnector].toInstance(mockEventReportingConnector),
    bind[AFTFrontendConnector].toInstance(mockAFTFrontendConnector),
    bind[FrontendAppConfig].toInstance(mockAppConfig),
  )

  override def beforeEach(): Unit = {
    reset(mockAppConfig)
    when(mockAppConfig.erOutstandingPaymentAmountURL).thenReturn(dummyUrl)
    super.beforeEach()
  }
  "EventReportingOverviewController" - {

    "must return OK and the correct view for a GET with feature toggle OFF" in {


      val application =  applicationBuilder(userAnswers = Some(ua), extraModules).build()

      when(mockUserAnswersCacheConnector.get(any()) (any(), any()))
        .thenReturn(Future.successful((Some(ua))))
      when(mockUserAnswersCacheConnector.get(any()) (any(), any()) )
        .thenReturn(Future.successful(None))

      when(mockUserAnswersCacheConnector.get(any(), any()) (any(), any()))
        .thenReturn(Future.successful((Some(ua))))

      when(mockUserAnswersCacheConnector.save(any(), any()) (any(), any()))
        .thenReturn(Future.successful(()))
      when(mockUserAnswersCacheConnector.removeAll(any())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockEventReportingConnector.getOverview(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful((Seq.empty)))
      when(mockAFTFrontendConnector.getErOutstandingPaymentAmount(any())(any(), any()))
        .thenReturn(Future(amountHtml))

      running(application) {


        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EventReportingOverviewView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(ovm)(request, messages(application)).toString
      }
    }
  }
}

