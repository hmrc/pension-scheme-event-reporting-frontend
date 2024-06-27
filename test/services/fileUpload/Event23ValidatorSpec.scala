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

class Event23ValidatorSpec extends BulkUploadSpec[Event23Validator] with BeforeAndAfterEach {
  //scalastyle:off magic.number

  "Event 23 validator" - {
    "return items in user answers when there are no validation errors" in {
      val data = s"""$header
                            Joe,Bloggs,AA234567D,2020 to 2023,12.20
                            Steven,Bloggs,AA123456C,2022 to 2023,13.20"""

      val ((output, errors), rowNumber) = validate(data)
      rowNumber mustBe 3
      errors.isEmpty mustBe true
      output.toJson mustBe Json.parse("""{
                                        |  "event23" : {
                                        |    "members" : [ {
                                        |      "chooseTaxYear" : "2020",
                                        |      "totalPensionAmounts" : 12.2,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA234567D"
                                        |      }
                                        |    }, {
                                        |      "chooseTaxYear" : "2022",
                                        |      "totalPensionAmounts" : 13.2,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Steven",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA123456C"
                                        |      }
                                        |    } ]
                                        |  }
                                        |}""".stripMargin)
    }

    "return validation errors when present, including tax year in future" in {
      val data = s"""$header
        ,Bloggs,AA234567D,2024,12.20
        Steven,,xyz,,
        Steven,Bloggs,AA123456C,2022 to 2023,13.20
        Steven,Bloggs,AA123456C,2022 to 2023,13.20"""

      val ((output, errors), rowNumber) = validate(data)
      errors mustBe Seq(
        ValidationError(1, 0, "membersDetails.error.firstName.required", "firstName"),
        ValidationError(1, 3, "chooseTaxYear.event22.error.outsideRange", "taxYear", Seq("2013", "2022")),
        ValidationError(2, 1, "membersDetails.error.lastName.required", "lastName"),
        ValidationError(2, 2, "genericNino.error.invalid.length", "nino"),
        ValidationError(2, 3, "chooseTaxYear.event23.error.required", "taxYear", Seq("2013", "2022")),
        ValidationError(2, 4, "totalPensionAmounts.value.error.nothingEntered", "totalAmounts")
      )
    }

  }

}
