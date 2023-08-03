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

import audit.AuditService
import base.SpecBase
import connectors.MinimalConnector.MinimalDetails
import connectors.{EmailConnector, EmailSent, EventReportingConnector, MinimalConnector}
import models.VersionInfo
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.enumeration.VersionStatus.Compiled
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, VersionInfoPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubmitService
import uk.gov.hmrc.http.ExpectationFailedException
import views.html.DeclarationView
import org.mockito.Mockito.doNothing
import play.api.http.Status.OK
import play.api.test.Helpers.GET
import org.scalatest.RecoverMethods._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class DeclarationControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val mockERConnector = mock[EventReportingConnector]
  private val mockAuditService = mock[AuditService]
  private val mockEmailConnector = mock[EmailConnector]
  private val mockMinimalConnector = mock[MinimalConnector]
  private val mockSubmitService = mock[SubmitService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[EventReportingConnector].toInstance(mockERConnector),
    bind[EmailConnector].toInstance(mockEmailConnector),
    bind[AuditService].toInstance(mockAuditService),
    bind[MinimalConnector].toInstance(mockMinimalConnector),
    bind[SubmitService].toInstance(mockSubmitService)
  )

  override protected def beforeEach(): Unit = {
    reset(mockERConnector)
    reset(mockAuditService)
    reset(mockEmailConnector)
    reset(mockMinimalConnector)
    reset(mockSubmitService)
  }

  "Declaration Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.DeclarationController.onPageLoad(waypoints).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(routes.DeclarationController.onClick(waypoints).url)(request, messages(application)).toString
      }
    }

    "must redirect to the correct page for method onClick" in {

      val testEmail = "test@test.com"
      val templateId = "pods_event_report_submitted"
      val organisationName = "Test company ltd"
      val minimalDetails = MinimalDetails(testEmail, false, Some(organisationName), None, false, false)

      when(mockERConnector.submitReport(any(), any(), any())(any(), any())).thenReturn(Future.successful(()))
      doNothing().when(mockAuditService).sendEvent(any())(any(), any())
      when(mockEmailConnector.sendEmail(
        schemeAdministratorType = ArgumentMatchers.eq(Administrator),
        requestId = any(), psaOrPspId = any(), pstr = any(),
        emailAddress = ArgumentMatchers.eq(testEmail),
        templateId = ArgumentMatchers.eq(templateId),
        templateParams = any(),
        reportVersion = any())(any(), any()))
        .thenReturn(Future.successful(EmailSent))
      when(mockMinimalConnector.getMinimalDetails(any(), any())(any(), any())).thenReturn(Future.successful(minimalDetails))
      when(mockSubmitService.submitReport(any(), any())(any(), any())).thenReturn(Future.successful(Ok))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Compiled))), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.DeclarationController.onClick(waypoints).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSubmittedController.onPageLoad(waypoints).url
      }
    }
  }
}