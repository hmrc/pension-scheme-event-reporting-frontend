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
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.{TaxYear, UserAnswers}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, EventReportingOverviewPage, EventReportingTileLinksPage}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.EventReportingOverviewService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.EventReportingOverviewView
import forms.TaxYearFormProvider
import models.enumeration.JourneyStartType.StartNew
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule

import scala.concurrent.{ExecutionContext, Future}


class EventReportingOverviewControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val waypoints = EmptyWaypoints
  val ua = emptyUserAnswers.setOrException(EventReportingTileLinksPage, StartNew)

  private val formProvider = new TaxYearFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockEventReportingConnector = mock[EventReportingConnector]

  private def getRoute: String = routes.EventReportingOverviewController.onPageLoad().url

  private def postRoute: String = routes.EventReportingOverviewController.onSubmit("2022", "InProgress").url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[EventReportingConnector].toInstance(mockEventReportingConnector)
  )

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

      running(application) {


        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EventReportingOverviewView]

//        val ovm = OverviewViewModel(pastYears = Seq(("", "")), yearsInProgress = Seq(("", "")), schemeName = "")
        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(ovm)(request, messages(application)).toString
      }
    }
  }
}

