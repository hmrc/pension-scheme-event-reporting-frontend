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

package viewmodels

import base.SpecBase
import play.api.libs.json.{JsPath, JsSuccess, Json}

class ValidationErrorForRenderingSpec extends SpecBase {
  "reads" - {
    "must transform correctly" in {
      val json = Json.obj(
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
      json.validate[Seq[ValidationErrorForRendering]](ValidationErrorForRendering.reads) mustBe JsSuccess(
        Seq(
          ValidationErrorForRendering(cell = "A1", error = "error1", columnName = "column1"),
          ValidationErrorForRendering(cell = "B2", error = "error2", columnName = "column2")
        ),
        JsPath \ "errors"
      )
    }
  }
}
