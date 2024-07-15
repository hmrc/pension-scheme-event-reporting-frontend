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

package controllers.fileUpload

import base.SpecBase
import connectors.ParsingAndValidationOutcomeCacheConnector
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event23, Event24, Event6}
import models.fileUpload.ParsingAndValidationOutcome
import models.fileUpload.ParsingAndValidationOutcomeStatus.Success
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fileUpload.FileUploadSuccessView

import scala.concurrent.Future

class FileUploadSuccessControllerSpec extends SpecBase with BeforeAndAfterEach {

  private def continueUrl(eventType: EventType) = s"/manage-pension-scheme-event-report/report/event-${eventType.toString}-summary"

  private val mockParsingAndValidationOutcomeCacheConnector = mock[ParsingAndValidationOutcomeCacheConnector]
  private val seqOfEvents = Seq(Event1, Event6, Event22, Event23, Event24)

  private val expectedOutcome = ParsingAndValidationOutcome(
    status = Success,
    json = Json.obj(),
    fileName = Some("filename.csv")
  )

  val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[ParsingAndValidationOutcomeCacheConnector].toInstance(mockParsingAndValidationOutcomeCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockParsingAndValidationOutcomeCacheConnector)
  }

  "FileUploadSuccess Controller" - {
    for (event <- seqOfEvents) {
      testReturnOkAndCorrectView(event)
    }
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET (Event ${eventType.toString})" in {

      when(mockParsingAndValidationOutcomeCacheConnector.getOutcome(any(), any()))
        .thenReturn(Future.successful(Some(expectedOutcome)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      running(application) {

        val request = FakeRequest(GET, routes.FileUploadSuccessController.onPageLoad(EmptyWaypoints, eventType).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[FileUploadSuccessView]

        val fileName = expectedOutcome.fileName.getOrElse("Your file")

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(continueUrl(eventType), fileName)(request, messages(application)).toString
      }
    }
  }
}
