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
import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event23, Event6}
import models.fileUpload.ParsingAndValidationOutcome
import models.fileUpload.ParsingAndValidationOutcomeStatus.ValidationErrorsMoreThanOrEqual10
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fileUpload.ValidationErrorsSummaryView

import scala.concurrent.Future

class ValidationErrorsSummaryControllerSpec extends SpecBase with BeforeAndAfterEach {
  private val seqOfEvents = Seq(Event1, Event6, Event22, Event23)
  private val mockParsingAndValidationOutcomeCacheConnector = mock[ParsingAndValidationOutcomeCacheConnector]
  private val dummyErrors: Seq[String] = Seq("Error1", "Error2", "Error3")

  private val expectedOutcome = ParsingAndValidationOutcome(
    status = ValidationErrorsMoreThanOrEqual10,
    json = Json.obj(
      "errors" -> Json.arr(
        JsString("Error1"),
        JsString("Error2"),
        JsString("Error3")
      ),
      "totalErrors" -> 3
    )
  )

  val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[ParsingAndValidationOutcomeCacheConnector].toInstance(mockParsingAndValidationOutcomeCacheConnector)
  )

  private def fileDownloadInstructionLink(eventType: EventType) = s"/manage-pension-scheme-event-report/event-${eventType.toString}-upload-format-instructions"

  private def returnUrl(eventType: EventType) = s"/manage-pension-scheme-event-report/report/event-${eventType.toString}-upload"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockParsingAndValidationOutcomeCacheConnector)
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET (Event ${eventType.toString})" in {

      when(mockParsingAndValidationOutcomeCacheConnector.getOutcome(any(), any())).thenReturn(Future.successful(Some(expectedOutcome)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      running(application) {

        val request = FakeRequest(GET, routes.ValidationErrorsSummaryController.onPageLoad(EmptyWaypoints, eventType).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ValidationErrorsSummaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(returnUrl(eventType),
          fileDownloadInstructionLink(eventType), dummyErrors, 3)(request, messages(application)).toString
      }
    }
  }

  "ValidationErrorsSummary Controller" - {
    for (event <- seqOfEvents) {
      testReturnOkAndCorrectView(event)
    }
  }


}
