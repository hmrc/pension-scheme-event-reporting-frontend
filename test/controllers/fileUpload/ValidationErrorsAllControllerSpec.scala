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
import models.fileUpload.ParsingAndValidationOutcome
import models.fileUpload.ParsingAndValidationOutcomeStatus.ValidationErrorsLessThan10
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.ValidationErrorForRendering
import views.html.fileUpload.ValidationErrorsAllView

import scala.concurrent.Future

class ValidationErrorsAllControllerSpec extends SpecBase {

  private val returnUrl = "/manage-pension-scheme-event-report/new-report/event-22-upload"
  private val fileDownloadInstructionLink = "/manage-pension-scheme-event-report/event-22-upload-format-instructions"
  private val mockParsingAndValidationOutcomeCacheConnector = mock[ParsingAndValidationOutcomeCacheConnector]
  private val expectedOutcome = ParsingAndValidationOutcome(
    status = ValidationErrorsLessThan10,
    json = Json.obj(
      "errors" -> Json.arr(
        Json.obj(
          "cell" -> "A1",
          "error" -> "error1",
          "columnName" -> "column1"
        ),
        Json.obj(
          "cell" -> "B2",
          "error" -> "error2",
          "columnName" -> "column2"
        )
      )
    )
  )


  val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[ParsingAndValidationOutcomeCacheConnector].toInstance(mockParsingAndValidationOutcomeCacheConnector)
  )

  "ValidationErrorsAll Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockParsingAndValidationOutcomeCacheConnector.getOutcome(any(), any()))
        .thenReturn(Future.successful(Some(expectedOutcome)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      running(application) {

        val request = FakeRequest(GET, routes.ValidationErrorsAllController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ValidationErrorsAllView]
        val dummyErrors = Seq(
          ValidationErrorForRendering(cell = "A1", error = "error1", columnName = "column1"),
          ValidationErrorForRendering(cell = "B2", error = "error2", columnName = "column2")
        )
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(returnUrl, fileDownloadInstructionLink, dummyErrors)(request, messages(application)).toString
      }
    }
  }
}
