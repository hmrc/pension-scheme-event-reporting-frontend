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

package models.fileUpload

import base.SpecBase
import play.api.libs.json.{JsString, Json}

class ParsingAndValidationOutcomeSpec extends SpecBase {

  "ParsingAndValidationOutcome" - {

    "must map correctly to ParsingAndValidationOutcome when status is Successful" in {

      val successfulOutcome =
        Json.obj(
          "status" -> "Success",
          "errors" -> Json.arr()
        )

      val result = successfulOutcome.as[ParsingAndValidationOutcome](ParsingAndValidationOutcome.reads)

      result.status mustBe ParsingAndValidationOutcomeStatus.Success
      result.lessThanTen.size mustBe 0
    }

    "must map correctly to ParsingAndValidationOutcome when status is GeneralError" in {

      val GeneralErrorOutcome =
        Json.obj(
          "status" -> "GeneralError",
          "errors" -> Json.arr()
        )

      val result = GeneralErrorOutcome.as[ParsingAndValidationOutcome](ParsingAndValidationOutcome.reads)

      result.status mustBe ParsingAndValidationOutcomeStatus.GeneralError
      result.lessThanTen.size mustBe 0
    }

    "must map correctly to ParsingAndValidationOutcome when status is ValidationErrorsLess10" in {

      val ValidationErrorsLess10 =
        Json.obj(
          "status" -> "ValidationErrorsLess10",
          "errors" -> Json.arr(
            Json.obj(
              "row" -> 1,
              "col" -> 2,
              "error" -> "Enter the member's first name",
              "columnName" -> "Test name"
            )
          )
        )

      val result = ValidationErrorsLess10.as[ParsingAndValidationOutcome](ParsingAndValidationOutcome.reads)

      result.status mustBe ParsingAndValidationOutcomeStatus.ValidationErrorsLessThan10
      result.lessThanTen.size mustBe 1
    }

    "must map correctly to ParsingAndValidationOutcome when status is ValidationErrorsLessMoreThanOrEqual10" in {

      val ValidationErrorsMoreThanOrEqual10 = Json.obj(
        "status" -> "ValidationErrorsMoreThanOrEqualTo10",
        "errors" -> Json.arr(
          JsString("Error1"),
          JsString("Error2"),
          JsString("Error3")
        )
      )

      val result = ValidationErrorsMoreThanOrEqual10.as[ParsingAndValidationOutcome](ParsingAndValidationOutcome.reads)

      result.status mustBe ParsingAndValidationOutcomeStatus.ValidationErrorsMoreThanOrEqual10
      result.lessThanTen.size mustBe 0
      result.moreThanTen.size mustBe 3
    }
  }
}
