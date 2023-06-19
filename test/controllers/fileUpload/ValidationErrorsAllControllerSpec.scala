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
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.fileUpload.ValidationError
import views.html.fileUpload.ValidationErrorsAllView

class ValidationErrorsAllControllerSpec extends SpecBase {

  private val returnUrl = "/manage-pension-scheme-event-report/new-report/event-22-upload"
  private val fileDownloadInstructionLink = "/manage-pension-scheme-event-report/event-22-upload-format-instructions"
  private val mockParsingAndValidationOutcomeCacheConnector = mock[ParsingAndValidationOutcomeCacheConnector]
  private val dummyErrors: Seq[ValidationError] = Seq(
    ValidationError(6, 1, "Enter the member's first name", "Column name"),
    ValidationError(5, 2, "Enter a National Insurance number that is 2 letters, 6 numbers, then A, B, C, or D, like QQ123456C", "Column name"),
    ValidationError(4, 3, "The charge amount must be an amount of money, like 123 or 123.45", "Column name"),
    ValidationError(3, 4, "Enter the date you received the notice to pay the charge", "Column name"),
    ValidationError(2, 5, "Select yes if the payment type is mandatory", "Column name"),
    ValidationError(1, 6, "Enter the tax year to which the annual allowance charge relates", "Column name")
  )

  val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[ParsingAndValidationOutcomeCacheConnector].toInstance(mockParsingAndValidationOutcomeCacheConnector)
  )

  "ValidationErrorsAll Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear)).build()

      running(application) {

        val request = FakeRequest(GET, routes.ValidationErrorsAllController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ValidationErrorsAllView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(returnUrl, fileDownloadInstructionLink, dummyErrors)(request, messages(application)).toString
      }
    }
  }
}
