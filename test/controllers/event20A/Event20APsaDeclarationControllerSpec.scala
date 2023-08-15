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

package controllers.event20A

import base.SpecBase
import connectors.MinimalConnector.{IndividualDetails, MinimalDetails}
import connectors.{EventReportingConnector, MinimalConnector}
import controllers.routes
import data.SampleData.sampleEvent20ABecameJourneyData
import models.VersionInfo
import models.enumeration.VersionStatus.{Compiled, Submitted}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{EmptyWaypoints, VersionInfoPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.event20A.Event20APsaDeclarationView

import scala.concurrent.Future

class Event20APsaDeclarationControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockERConnector = mock[EventReportingConnector]
  private val mockMinimalConnector = mock[MinimalConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[EventReportingConnector].toInstance(mockERConnector),
    bind[MinimalConnector].toInstance(mockMinimalConnector)
  )

  override protected def beforeEach(): Unit = {
    reset(mockERConnector)
    reset(mockMinimalConnector)
  }

  "Event20APsaDeclaration Controller" - {

    val schemeName = "schemeName"
    val pstr = "87219363YN"
    val taxYear = "2022"
    val adminName = "John Smith"

    "must return OK and the correct view for a GET when when isReportSubmitted is false" in {

      val testEmail = "test@test.com"
      val userAnswersWithVersionInfo = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()
      val minimalDetails = {
        MinimalDetails(testEmail, false, None, Some(IndividualDetails(firstName = "John", None, lastName = "Smith")), false, false)
      }

      running(application) {
        when(mockMinimalConnector.getMinimalDetails(any(), any())(any(), any())).thenReturn(Future.successful(minimalDetails))
        val request = FakeRequest(GET, controllers.event20A.routes.Event20APsaDeclarationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[Event20APsaDeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          schemeName, pstr, taxYear, adminName, controllers.event20A.routes.Event20APsaDeclarationController.onClick(EmptyWaypoints).url)(request, messages(application)
        ).toString
      }
    }

    "must redirect to cannot resume page when isReportSubmitted is true" in {

      val testEmail = "test@test.com"
      val userAnswersWithVersionInfo = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Submitted))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()
      val minimalDetails = {
        MinimalDetails(testEmail, false, None, Some(IndividualDetails(firstName = "John", None, lastName = "Smith")), false, false)
      }

      running(application) {
        when(mockMinimalConnector.getMinimalDetails(any(), any())(any(), any())).thenReturn(Future.successful(minimalDetails))
        val request = FakeRequest(GET, controllers.event20A.routes.Event20APsaDeclarationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CannotResumeController.onPageLoad(EmptyWaypoints).url
      }
    }

    "must redirect to the correct page for method onClick" in {

      val testEmail = "test@test.com"
      val organisationName = "Test company ltd"
      val minimalDetails = MinimalDetails(testEmail, false, Some(organisationName), None, false, false)

      when(mockMinimalConnector.getMinimalDetails(any(), any())(any(), any())).thenReturn(Future.successful(minimalDetails))
      when(mockERConnector.submitReportEvent20A(any(), any(), any())(any())).thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(sampleEvent20ABecameJourneyData), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.event20A.routes.Event20APsaDeclarationController.onClick(EmptyWaypoints).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url
      }
    }
  }
}
