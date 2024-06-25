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

class Event22ValidatorSpec extends BulkUploadSpec[Event22Validator] with BeforeAndAfterEach {
  //scalastyle:off magic.number

  "Event 22 validator" - {
    "return items in user answers when there are no validation errors" in {
      val data = s"""$header
                            Joe,Bloggs,AA234567D,2020 to 2023,12.20
                            Steven,Bloggs,AA123456C,2022 to 2023,13.20"""

      val ((output, errors), rowNumber) = validate(data)

      //TODO: Figure out if the below is correct
      println(Json.prettyPrint(output.toJson))
      rowNumber mustBe 3
      errors.isEmpty mustBe true
    }

    // The test below passes fine but it is unnecessary to run each time. It serves though as a useful prototype
    // for when we are doing load testing. It generates 10K rows and parses/ validates them.
//    "return correctly and in timely fashion (< 30 seconds) when there is a large payload (10K items)" in {
//      val payloadMain = (1 to 20000).foldLeft(""){ (acc, c) =>
//        val nino = "AA" + ("00000" + c.toString).takeRight(6) + "C"
//        acc + """
//""" + s"""Joe,Bloggs,$nino,2020 to 2023,12.20"""
//      }
//
//      val validCSVFile = CSVParser.split(
//        s"""$header
//""" + payloadMain
//      )
//      val ua = UserAnswers().setOrException(TaxYearPage, TaxYear("2023"), nonEventTypeData = true)
//      val startTime = System.currentTimeMillis
//      val result = validator.validate(validCSVFile, ua)
//      val endTime = System.currentTimeMillis
//      val timeTaken = (endTime - startTime) / 1000
//      result.isValid mustBe true
//      //println(s"Validated large payload (took $timeTaken seconds)")
//      if (timeTaken < 30) {
//        assert(true, s"Validated large payload in less than 30 seconds (took $timeTaken seconds)")
//      } else {
//        assert(false, s"Validated large payload in more than 30 seconds (actually took $timeTaken seconds)")
//      }
//    }

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
        ValidationError(2, 3, "chooseTaxYear.event22.error.required", "taxYear", Seq("2013", "2022")),
        ValidationError(2, 4, "totalPensionAmounts.value.error.nothingEntered", "totalAmounts")
      )
    }

  }
}
