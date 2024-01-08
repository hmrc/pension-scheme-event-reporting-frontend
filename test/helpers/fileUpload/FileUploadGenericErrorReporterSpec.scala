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

package helpers.fileUpload

import base.SpecBase
import models.enumeration.EventType
import org.scalatest.matchers.must.Matchers
import services.fileUpload.ValidationError

class FileUploadGenericErrorReporterSpec extends SpecBase with Matchers {

  "File upload generic error reporter" - {
    "return generic list of errors for the failed event 22" in {
      val underTest = FileUploadGenericErrorReporter

      val errors = Seq(
        ValidationError(1, 0, "memberDetails.error.firstName.required", "firstName"),
        ValidationError(2, 1, "memberDetails.error.lastName.required", "lastName"),
        ValidationError(2, 2, "memberDetails.error.nino.invalid", "nino"),
        ValidationError(2, 3, "chooseTaxYear.event22.error.required", "taxYear"),
        ValidationError(2, 4, "totalPensionAmounts.value.error.nothingEntered", "totalAmounts")
      )

      val result = underTest.generateGenericErrorReport(errors, EventType.Event22)

      result mustBe List("fileUpload.memberDetails.generic.error.firstName",
        "fileUpload.memberDetails.generic.error.lastName",
        "fileUpload.memberDetails.generic.error.nino",
        "fileUpload.taxYear.generic.error",
        "fileUpload.totalAmounts.generic.error"
      )
    }
  }

}
