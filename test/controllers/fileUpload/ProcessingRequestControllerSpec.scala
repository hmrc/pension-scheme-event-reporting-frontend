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

package controllers.fileUpload

import base.SpecBase
import connectors.ParsingAndValidationOutcomeCacheConnector
import models.fileUpload.{ParsingAndValidationOutcome, ParsingAndValidationOutcomeStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fileUpload.ProcessingRequestView

import scala.concurrent.Future

class ProcessingRequestControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockParsingAndValidationOutcomeCacheConnector = mock[ParsingAndValidationOutcomeCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq(
    bind[ParsingAndValidationOutcomeCacheConnector].to(mockParsingAndValidationOutcomeCacheConnector)
  )
  private val waypoints = EmptyWaypoints

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockParsingAndValidationOutcomeCacheConnector)
  }

  "Processing Request Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.ProcessingRequestController.onPageLoad(waypoints).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProcessingRequestView]

        contentAsString(result) mustEqual view(controllers.routes.IndexController.onPageLoad.url)(request, messages(application)).toString
      }
    }

    "return OK and the correct view for a GET when outcome is Success and can get file name" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      val testFile = "test-file.csv"
      when(mockParsingAndValidationOutcomeCacheConnector.getOutcome(any(), any()))
        .thenReturn(Future.successful(Some(ParsingAndValidationOutcome(status = ParsingAndValidationOutcomeStatus.Success, fileName = Some(testFile)))))

      val request = FakeRequest(GET, routes.ProcessingRequestController.onPageLoad(waypoints).url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.FileUploadSuccessController.onPageLoad(waypoints).url
      verify(mockParsingAndValidationOutcomeCacheConnector, times(1)).getOutcome(any(), any())
    }
  }
}
