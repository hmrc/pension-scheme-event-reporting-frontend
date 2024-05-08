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

package services.fileUpload

import base.BulkUploadSpec
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json

class Event6ValidatorSpec extends BulkUploadSpec[Event6Validator] with BeforeAndAfterEach {
  //scalastyle:off magic.number

  "Event 6 validator" - {
    "return items in user answers when there are no validation errors" in {
      val data =s"""$header
                            Joe,Bloggs,AA234567D,enhanced lifetime allowance,1234567A,10.00,08/11/2022
                            Steven,Bloggs,AA123456C,fixed protection 2014,1234567A,10.00,12/08/2022"""

      val ((output, errors), rowNumber) = validate(data)

      //TODO: Figure out if the below is correct
      println(Json.prettyPrint(output.toJson))
      rowNumber mustBe 3
      errors.isEmpty mustBe true
    }

    "return validation errors when present, including tax year in future" in {
      val data = s"""$header
        Joe,,AA234567D,enhanced lifetime allowance,,12.20,08/11/2022
        Joe,Bloggs,,Enhanced lifetime allowance,12345678,12.20,08/11/2026
        Steven,Bloggs,AA234567D,fixed protection 2014,1234567A,10.00,12/08/2022
        Steven,Bloggs,AA234567D,fixed protection 2014,1234567A,10.00,12/08/2022"""

      val ((output, errors), rowNumber) = validate(data)
      errors.toSeq mustBe Seq(
        ValidationError(1, 1, "membersDetails.error.lastName.required", "lastName"),
        ValidationError(1, 4, "inputProtectionType.error.required", "typeOfProtectionReference"),
        ValidationError(2, 2, "membersDetails.error.nino.required", "nino"),
        ValidationError(2, 3, "typeOfProtection.error.format", "typeOfProtection"),
        ValidationError(2, 6, "Date must be between 06 April 2006 and 05 April 2023", "crystallisedDate")
      )
    }
  }

}