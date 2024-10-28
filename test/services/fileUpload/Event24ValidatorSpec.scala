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

import scala.collection.immutable.ArraySeq

class Event24ValidatorSpec extends BulkUploadSpec[Event24Validator](2023) with BeforeAndAfterEach {

  "Event 24 Validator" - {
    "return a valid result if there are no validation errors" in {
      val data =s"""$header
                            ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                            ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM",12384nd82js,,123hids892h,,,NO,YES,,YES,,123/ABCDEF
                            ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,,,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                            ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,,,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,NO,,
                            ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,YES,,,YES,,123/ABCDEF
                            ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,YES,,,NO,,"""

      val ((output, errors), rowNumber) = validate(data)
      rowNumber mustBe 7
      errors.isEmpty mustBe true
      output.toJson mustBe Json.parse("""{
                                        |  "event24" : {
                                        |    "members" : [ {
                                        |      "overAllowance" : true,
                                        |      "overAllowanceAndDeathBenefit" : false,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Jane",
                                        |        "lastName" : "Doe",
                                        |        "nino" : "AB123456A"
                                        |      },
                                        |      "typeOfProtectionGroup1Reference" : {
                                        |        "nonResidenceEnhancement" : "12384nd82js",
                                        |        "pensionCreditsPreCRE" : "",
                                        |        "preCommencement" : "123hids892h",
                                        |        "recognisedOverseasPSTE" : ""
                                        |      },
                                        |      "bceTypeSelection" : "annuityProtection",
                                        |      "typeOfProtectionGroup2" : "fixedProtection",
                                        |      "typeOfProtectionGroup1" : [ "nonResidenceEnhancement", "preCommencement", "schemeSpecific" ],
                                        |      "typeOfProtectionGroup2Reference" : "abcdef123",
                                        |      "totalAmountBenefitCrystallisation" : 12300,
                                        |      "crystallisedDate" : {
                                        |        "date" : "2023-11-13"
                                        |      },
                                        |      "validProtection" : true
                                        |    }, {
                                        |      "overAllowance" : true,
                                        |      "overAllowanceAndDeathBenefit" : false,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Jane",
                                        |        "lastName" : "Doe",
                                        |        "nino" : "AB123456A"
                                        |      },
                                        |      "typeOfProtectionGroup1Reference" : {
                                        |        "nonResidenceEnhancement" : "12384nd82js",
                                        |        "pensionCreditsPreCRE" : "",
                                        |        "preCommencement" : "123hids892h",
                                        |        "recognisedOverseasPSTE" : ""
                                        |      },
                                        |      "bceTypeSelection" : "annuityProtection",
                                        |      "typeOfProtectionGroup2" : "fixedProtection",
                                        |      "typeOfProtectionGroup1" : [ "nonResidenceEnhancement", "preCommencement" ],
                                        |      "typeOfProtectionGroup2Reference" : "abcdef123",
                                        |      "totalAmountBenefitCrystallisation" : 12300,
                                        |      "crystallisedDate" : {
                                        |        "date" : "2023-11-13"
                                        |      },
                                        |      "validProtection" : true
                                        |    }, {
                                        |      "overAllowance" : true,
                                        |      "overAllowanceAndDeathBenefit" : false,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Jane",
                                        |        "lastName" : "Doe",
                                        |        "nino" : "AB123456A"
                                        |      },
                                        |      "typeOfProtectionGroup1Reference" : {
                                        |        "nonResidenceEnhancement" : "12384nd82js",
                                        |        "pensionCreditsPreCRE" : "",
                                        |        "preCommencement" : "123hids892h",
                                        |        "recognisedOverseasPSTE" : ""
                                        |      },
                                        |      "bceTypeSelection" : "annuityProtection",
                                        |      "typeOfProtectionGroup2" : "noOtherProtections",
                                        |      "typeOfProtectionGroup1" : [ "nonResidenceEnhancement", "preCommencement", "schemeSpecific" ],
                                        |      "totalAmountBenefitCrystallisation" : 12300,
                                        |      "crystallisedDate" : {
                                        |        "date" : "2023-11-13"
                                        |      },
                                        |      "validProtection" : true
                                        |    }, {
                                        |      "overAllowance" : true,
                                        |      "overAllowanceAndDeathBenefit" : false,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Jane",
                                        |        "lastName" : "Doe",
                                        |        "nino" : "AB123456A"
                                        |      },
                                        |      "typeOfProtectionGroup1Reference" : {
                                        |        "nonResidenceEnhancement" : "12384nd82js",
                                        |        "pensionCreditsPreCRE" : "",
                                        |        "preCommencement" : "123hids892h",
                                        |        "recognisedOverseasPSTE" : ""
                                        |      },
                                        |      "bceTypeSelection" : "annuityProtection",
                                        |      "typeOfProtectionGroup2" : "noOtherProtections",
                                        |      "typeOfProtectionGroup1" : [ "nonResidenceEnhancement", "preCommencement", "schemeSpecific" ],
                                        |      "totalAmountBenefitCrystallisation" : 12300,
                                        |      "crystallisedDate" : {
                                        |        "date" : "2023-11-13"
                                        |      },
                                        |      "validProtection" : true
                                        |    }, {
                                        |      "overAllowanceAndDeathBenefit" : true,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Jane",
                                        |        "lastName" : "Doe",
                                        |        "nino" : "AB123456A"
                                        |      },
                                        |      "typeOfProtectionGroup1Reference" : {
                                        |        "nonResidenceEnhancement" : "12384nd82js",
                                        |        "pensionCreditsPreCRE" : "",
                                        |        "preCommencement" : "123hids892h",
                                        |        "recognisedOverseasPSTE" : ""
                                        |      },
                                        |      "bceTypeSelection" : "annuityProtection",
                                        |      "typeOfProtectionGroup2" : "fixedProtection",
                                        |      "typeOfProtectionGroup1" : [ "nonResidenceEnhancement", "preCommencement", "schemeSpecific" ],
                                        |      "typeOfProtectionGroup2Reference" : "abcdef123",
                                        |      "totalAmountBenefitCrystallisation" : 12300,
                                        |      "crystallisedDate" : {
                                        |        "date" : "2023-11-13"
                                        |      },
                                        |      "validProtection" : true
                                        |    }, {
                                        |      "overAllowanceAndDeathBenefit" : true,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Jane",
                                        |        "lastName" : "Doe",
                                        |        "nino" : "AB123456A"
                                        |      },
                                        |      "typeOfProtectionGroup1Reference" : {
                                        |        "nonResidenceEnhancement" : "12384nd82js",
                                        |        "pensionCreditsPreCRE" : "",
                                        |        "preCommencement" : "123hids892h",
                                        |        "recognisedOverseasPSTE" : ""
                                        |      },
                                        |      "bceTypeSelection" : "annuityProtection",
                                        |      "typeOfProtectionGroup2" : "fixedProtection",
                                        |      "typeOfProtectionGroup1" : [ "nonResidenceEnhancement", "preCommencement", "schemeSpecific" ],
                                        |      "typeOfProtectionGroup2Reference" : "abcdef123",
                                        |      "totalAmountBenefitCrystallisation" : 12300,
                                        |      "crystallisedDate" : {
                                        |        "date" : "2023-11-13"
                                        |      },
                                        |      "validProtection" : true
                                        |    } ]
                                        |  }
                                        |}
                                        |""".stripMargin)
    }

    "return validation errors when tax year out of range" in {
      val data = s"""$header
                          ,Jane,Doe,AB123456A,13/11/2026,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF"""

      val ((output, errors), rowNumber) = validate(data)

      errors mustBe Seq(
        ValidationError(1, 4, "Date must be between 06 April 2023 and 05 April 2024", "crystallisedDate")
      )
    }

    "return validation errors if present" in {
      val data = s"""$header
                          ,,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/202,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,ANNI,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,ANN,YES,YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXEDO,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123dnskassubcb,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",NO,,,,,,,,,,,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,,YES,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,ANN,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,,,YES,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,STAND,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,,,123/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,STAND,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,12:/ABCDEF
                          ,Jane,Doe,AB123456A,13/11/2023,STAND,"123,00",YES,,FIXED,abcdef123,"NON-RESIDENCE,PRE-COMM,SS",12384nd82js,,123hids892h,,YES,NO,YES,,YES,,"""


      val ((output, errors), rowNumber) = validate(data)

      errors mustBe Seq(
        ValidationError(1, 1, "membersDetails.error.firstName.required", "firstName"),
        ValidationError(2, 2, "membersDetails.error.lastName.required", "lastName"),
        ValidationError(3, 3, "membersDetails.error.nino.required", "nino"),
        ValidationError(4, 4, "genericDate.error.invalid.allFieldsMissing", "crystallisedDate"),
        ValidationError(5, 4, "genericDate.error.invalid.year", "crystallisedDate"),
        ValidationError(6, 5, "bceTypeSelection.error.format", "bceType"),
        ValidationError(7, 6, "totalAmountBenefitCrystallisation.event24.error.nonNumeric", "totalAmount"),
        ValidationError(8, 7, "validProtection.event24.error.required", "validProtection"),
        ValidationError(9, 12, "typeOfProtectionReference.error.required", "nonResidenceEnhancement"),
        ValidationError(10, 9, "typeOfProtection.event24.error.format", "protectionTypeGroup2"),
        ValidationError(11, 10, "typeOfProtectionReference.event24.error.maxLength",
          "protectionTypeGroup2Reference", ArraySeq(15)),
        ValidationError(12, 17, "overAllowanceAndDeathBenefit.event24.error.required", "overAllowanceAndDeathBenefit"),
        ValidationError(13, 17, "overAllowanceAndDeathBenefit.event24.error.required", "overAllowanceAndDeathBenefit"),
        ValidationError(14, 18, "overAllowance.event24.error.required", "overAllowance"),
        ValidationError(15, 20, "marginalRate.event24.error.required", "marginalRate"),
        ValidationError(16, 22, "employerPayeReference.event24.error.disallowedChars", "employerPayeRef",
          ArraySeq("[A-Za-z0-9/]{9,12}")),
        ValidationError(17, 22, "employerPayeReference.event24.error.required", "employerPayeRef")
      )
    }
  }
}
