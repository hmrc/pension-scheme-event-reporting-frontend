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

class Event1ValidatorSpec extends BulkUploadSpec[Event1Validator] with BeforeAndAfterEach {
  //scalastyle:off magic.number

  private val validAddress = "10 Other Place,Some District,Anytown,Anyplace,ZZ1 1ZZ,UK"
  private val commonUaEmployer = "employer,,,,,,,Company Name,12345678"
  private val moreThanMax: String = "a" * 161
  private val moreThanMaxAndSpecialChar: String = "a" * 161 + "%^&"

  "Event 1 validator" - {
    "must return items in user answers when there are no validation errors for Member" in {
      val data =
        s"""$header
                                member,Joe,Bloggs,AA123456A,YES,YES,YES,,,,BENEFIT,Description,,,,,,,,,,,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA123456B,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1000.00,08/11/2022
                                member,Joe,Bloggs,AA123456C,YES,NO,,,,,ERROR,,,,Description,,,,,,,,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA123456D,YES,YES,NO,,,,EARLY,,,Description,,,,,,,,,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA234567A,YES,YES,YES,,,,REFUND,,,,,,,,,WIDOW/ORPHAN,,,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA234567B,YES,YES,NO,,,,OVERPAYMENT,,,,,,,,NO LONGER QUALIFIED,,,,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA234567C,YES,YES,NO,,,,RESIDENTIAL,,,,,,,,,,"$validAddress",,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA234567D,YES,YES,YES,,,,TANGIBLE,,,,,,,,,,,Description,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA345678A,YES,YES,YES,,,,COURT,,John,,,,,,,,,,,,1000.00,08/11/2022
                                member,Joe,Bloggs,AA345678B,YES,NO,,,,,OTHER,,,,,,,Description,,,,,,,1000.00,08/11/2022
                                $commonUaEmployer,"$validAddress",LOANS,,,,,10.00,20.57,,,,,,,,1000.00,08/11/2022
                                $commonUaEmployer,"$validAddress",RESIDENTIAL,,,,,,,,,,"$validAddress",,,,1000.00,08/11/2022
                                $commonUaEmployer,"$validAddress",TANGIBLE,,,,,,,,,,,Description,,,1000.00,08/11/2022
                                $commonUaEmployer,"$validAddress",COURT,,Organisation Name,,,,,,,,,,,,1000.00,08/11/2022
                                $commonUaEmployer,"$validAddress",OTHER,,,,,,,Description,,,,,,,1000.00,08/11/2022"""
      val ((output, errors), rowNumber) = validate(data)

      rowNumber mustBe 16
      errors.isEmpty mustBe true
      output.toJson mustBe Json.parse("""{
                                        |  "event1" : {
                                        |    "membersOrEmployers" : [ {
                                        |      "doYouHoldSignedMandate" : true,
                                        |      "schemeUnAuthPaySurchargeMember" : true,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA123456A"
                                        |      },
                                        |      "whoReceivedUnauthPayment" : "member",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureMember" : "benefitInKind",
                                        |      "benefitInKindBriefDescription" : "Description",
                                        |      "valueOfUnauthorisedPayment" : true
                                        |    }, {
                                        |      "schemeDetails" : {
                                        |        "schemeName" : "SchemeName",
                                        |        "reference" : "SchemeReference"
                                        |      },
                                        |      "doYouHoldSignedMandate" : true,
                                        |      "schemeUnAuthPaySurchargeMember" : false,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA123456B"
                                        |      },
                                        |      "whoWasTheTransferMade" : "anEmployerFinanced",
                                        |      "whoReceivedUnauthPayment" : "member",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureMember" : "transferToNonRegPensionScheme",
                                        |      "valueOfUnauthorisedPayment" : true
                                        |    }, {
                                        |      "doYouHoldSignedMandate" : true,
                                        |      "errorDescription" : "Description",
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA123456C"
                                        |      },
                                        |      "whoReceivedUnauthPayment" : "member",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureMember" : "errorCalcTaxFreeLumpSums",
                                        |      "valueOfUnauthorisedPayment" : false
                                        |    }, {
                                        |      "doYouHoldSignedMandate" : true,
                                        |      "schemeUnAuthPaySurchargeMember" : false,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA123456D"
                                        |      },
                                        |      "benefitsPaidEarly" : "Description",
                                        |      "whoReceivedUnauthPayment" : "member",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureMember" : "benefitsPaidEarly",
                                        |      "valueOfUnauthorisedPayment" : true
                                        |    }, {
                                        |      "doYouHoldSignedMandate" : true,
                                        |      "schemeUnAuthPaySurchargeMember" : true,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA234567A"
                                        |      },
                                        |      "refundOfContributions" : "widowOrOrphan",
                                        |      "whoReceivedUnauthPayment" : "member",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureMember" : "refundOfContributions",
                                        |      "valueOfUnauthorisedPayment" : true
                                        |    }, {
                                        |      "doYouHoldSignedMandate" : true,
                                        |      "schemeUnAuthPaySurchargeMember" : false,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA234567B"
                                        |      },
                                        |      "whoReceivedUnauthPayment" : "member",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureMember" : "overpaymentOrWriteOff",
                                        |      "reasonForTheOverpaymentOrWriteOff" : "dependentNoLongerQualifiedForPension",
                                        |      "valueOfUnauthorisedPayment" : true
                                        |    }, {
                                        |      "doYouHoldSignedMandate" : true,
                                        |      "schemeUnAuthPaySurchargeMember" : false,
                                        |      "memberResidentialAddress" : {
                                        |        "address" : {
                                        |          "addressLine1" : "10 Other Place",
                                        |          "addressLine2" : "Some District",
                                        |          "townOrCity" : "Anytown",
                                        |          "county" : "Anyplace",
                                        |          "postcode" : "ZZ1 1ZZ",
                                        |          "country" : "GB"
                                        |        }
                                        |      },
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA234567C"
                                        |      },
                                        |      "whoReceivedUnauthPayment" : "member",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureMember" : "residentialPropertyHeld",
                                        |      "valueOfUnauthorisedPayment" : true
                                        |    }, {
                                        |      "doYouHoldSignedMandate" : true,
                                        |      "schemeUnAuthPaySurchargeMember" : true,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA234567D"
                                        |      },
                                        |      "whoReceivedUnauthPayment" : "member",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "memberTangibleMoveableProperty" : "Description",
                                        |      "paymentNatureMember" : "tangibleMoveablePropertyHeld",
                                        |      "valueOfUnauthorisedPayment" : true
                                        |    }, {
                                        |      "doYouHoldSignedMandate" : true,
                                        |      "schemeUnAuthPaySurchargeMember" : true,
                                        |      "unauthorisedPaymentRecipientName" : "John",
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA345678A"
                                        |      },
                                        |      "whoReceivedUnauthPayment" : "member",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureMember" : "courtOrConfiscationOrder",
                                        |      "valueOfUnauthorisedPayment" : true
                                        |    }, {
                                        |      "memberPaymentNatureDescription" : "Description",
                                        |      "doYouHoldSignedMandate" : true,
                                        |      "membersDetails" : {
                                        |        "firstName" : "Joe",
                                        |        "lastName" : "Bloggs",
                                        |        "nino" : "AA345678B"
                                        |      },
                                        |      "whoReceivedUnauthPayment" : "member",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureMember" : "memberOther",
                                        |      "valueOfUnauthorisedPayment" : false
                                        |    }, {
                                        |      "event1" : {
                                        |        "companyDetails" : {
                                        |          "companyName" : "Company Name",
                                        |          "companyNumber" : "12345678"
                                        |        }
                                        |      },
                                        |      "employerAddress" : {
                                        |        "address" : {
                                        |          "addressLine1" : "10 Other Place",
                                        |          "addressLine2" : "Some District",
                                        |          "townOrCity" : "Anytown",
                                        |          "county" : "Anyplace",
                                        |          "postcode" : "ZZ1 1ZZ",
                                        |          "country" : "GB"
                                        |        }
                                        |      },
                                        |      "loanDetails" : {
                                        |        "loanAmount" : 10,
                                        |        "fundValue" : 20.57
                                        |      },
                                        |      "whoReceivedUnauthPayment" : "employer",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureEmployer" : "loansExceeding50PercentOfFundValue"
                                        |    }, {
                                        |      "event1" : {
                                        |        "companyDetails" : {
                                        |          "companyName" : "Company Name",
                                        |          "companyNumber" : "12345678"
                                        |        }
                                        |      },
                                        |      "employerAddress" : {
                                        |        "address" : {
                                        |          "addressLine1" : "10 Other Place",
                                        |          "addressLine2" : "Some District",
                                        |          "townOrCity" : "Anytown",
                                        |          "county" : "Anyplace",
                                        |          "postcode" : "ZZ1 1ZZ",
                                        |          "country" : "GB"
                                        |        }
                                        |      },
                                        |      "whoReceivedUnauthPayment" : "employer",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureEmployer" : "residentialProperty",
                                        |      "employerResidentialAddress" : {
                                        |        "address" : {
                                        |          "addressLine1" : "10 Other Place",
                                        |          "addressLine2" : "Some District",
                                        |          "townOrCity" : "Anytown",
                                        |          "county" : "Anyplace",
                                        |          "postcode" : "ZZ1 1ZZ",
                                        |          "country" : "GB"
                                        |        }
                                        |      }
                                        |    }, {
                                        |      "event1" : {
                                        |        "companyDetails" : {
                                        |          "companyName" : "Company Name",
                                        |          "companyNumber" : "12345678"
                                        |        }
                                        |      },
                                        |      "employerTangibleMoveableProperty" : "Description",
                                        |      "employerAddress" : {
                                        |        "address" : {
                                        |          "addressLine1" : "10 Other Place",
                                        |          "addressLine2" : "Some District",
                                        |          "townOrCity" : "Anytown",
                                        |          "county" : "Anyplace",
                                        |          "postcode" : "ZZ1 1ZZ",
                                        |          "country" : "GB"
                                        |        }
                                        |      },
                                        |      "whoReceivedUnauthPayment" : "employer",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureEmployer" : "tangibleMoveableProperty"
                                        |    }, {
                                        |      "event1" : {
                                        |        "companyDetails" : {
                                        |          "companyName" : "Company Name",
                                        |          "companyNumber" : "12345678"
                                        |        }
                                        |      },
                                        |  "employerAddress" : {
                                        |        "address" : {
                                        |          "addressLine1" : "10 Other Place",
                                        |          "addressLine2" : "Some District",
                                        |          "townOrCity" : "Anytown",
                                        |          "county" : "Anyplace",
                                        |          "postcode" : "ZZ1 1ZZ",
                                        |          "country" : "GB"
                                        |        }
                                        |      },
                                        |      "unauthorisedPaymentRecipientName" : "Organisation Name",
                                        |      "whoReceivedUnauthPayment" : "employer",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureEmployer" : "courtOrder"
                                        |    }, {
                                        |      "event1" : {
                                        |        "companyDetails" : {
                                        |          "companyName" : "Company Name",
                                        |          "companyNumber" : "12345678"
                                        |        }
                                        |      },
                                        |      "employerAddress" : {
                                        |        "address" : {
                                        |          "addressLine1" : "10 Other Place",
                                        |          "addressLine2" : "Some District",
                                        |          "townOrCity" : "Anytown",
                                        |          "county" : "Anyplace",
                                        |          "postcode" : "ZZ1 1ZZ",
                                        |          "country" : "GB"
                                        |        }
                                        |      },
                                        |      "employerPaymentNatureDescription" : "Description",
                                        |      "whoReceivedUnauthPayment" : "employer",
                                        |      "paymentValueAndDate" : {
                                        |        "paymentValue" : 1000,
                                        |        "paymentDate" : "2022-11-08"
                                        |      },
                                        |      "paymentNatureEmployer" : "employerOther"
                                        |    } ]
                                        |  }
                                        |}""".stripMargin)

    }

    // The test below passes fine but it is unnecessary to run each time. It serves though as a useful prototype
    // for when we are doing load testing. It generates 10K rows and parses/ validates them.
//        "return correctly and in timely fashion (< 30 seconds) when there is a large payload (10K items)" in {
//          val payloadMain = (1 to 20000).foldLeft("") { (acc, c) =>
//            val nino = "AA" + ("00000" + c.toString).takeRight(6) + "C"
//            acc +
//              """
//    """ + s"""member,Joe,Bloggs,$nino,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1000.00,08/11/2022"""
//          }
//
//          val data = s"""$header
//            """ + payloadMain
//          val startTime = System.currentTimeMillis
//          val ((output, errors), rowNumber) = validate(data)
//          val endTime = System.currentTimeMillis
//          val timeTaken = (endTime - startTime) / 1000
//          errors.isEmpty mustBe true
//          println(s"Validated large payload (took $timeTaken seconds)")
//          if (timeTaken < 30) {
//            assert(true, s"Validated large payload in less than 30 seconds (took $timeTaken seconds)")
//          } else {
//            assert(false, s"Validated large payload in more than 30 seconds (actually took $timeTaken seconds)")
//          }
//        }

    "return validation errors when present (Member)" in {

      val data =
        s"""$header
                        dsfgsd*,Joe,Bloggs,AA234567D,YES,YES,YES,,,,BENEFIT,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,,Bloggs12213,AA234567Dasdfsdf,YES,YES,YES,,,,BENEFIT,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA234567A,,,YES,,,,BENEFIT,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA234567B,YES,YES,,,,,BENEFIT,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        ,Joe,Bloggs,AA234567C,YES,YES,YES,,,,BENEFIT,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA123456A,YES,YES,YES,,,,,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA123456B,YES,YES,YES,,,,BENEFIT,$moreThanMax,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Steven,Bloggs,AA394821C,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,,"SchemeName,SchemeReference",1000.00,08/11/2022
                        member,Joe,Bloggs,AA910792D,YESasdf,YESasdf,NO,,,,BENEFIT,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA911842D,YES,YES,sdf,,,,BENEFIT,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA810238D,YES,YES,YES,,,,Benefitadfadf,Description,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Steven,Bloggs,AA995196C,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,sfasdf!2,"SchemeName,SchemeReference",1000.00,08/11/2022
                        member,Steven,Bloggs,AA819927C,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"$moreThanMax,$moreThanMax",1000.00,08/11/2022
                        member,Joe,Bloggs,AA882118D,YES,NO,,,,,ERROR,,,,$moreThanMax,,,,,,,,,,1000.00,08/11/2022
                        member,Steven,Bloggs,AA881753C,YES,YES,YES,,,,TANGIBLE,,,,,,,,,,,$moreThanMax,,,1000.00,08/11/2022
                        member,Steven,Bloggs,AA911736C,YES,NO,,,,,OTHER,,,,,,,$moreThanMax,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA911058D,YES,YES,YES,,,,REFUND,,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA388401D,YES,YES,YES,,,,REFUND,,,,,,,,,WIDOW/ORPHANsdfgsdf,,,,,1000.00,08/11/2022
                        member,Steven,Bloggs,AA488195C,YES,YES,NO,,,,OVERPAYMENT,,,,,,,,,,,,,,1000.00,08/11/2022
                        member,Steven,Bloggs,AA711924C,YES,YES,NO,,,,OVERPAYMENT,,,,,,,,ajsf%245,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA833964D,YES,YES,YES,,,,COURT,,$moreThanMax,,,,,,,,,,,,1000.00,08/11/2022
                        member,Joe,Bloggs,AA901167D,YES,YES,YES,,,,COURT,,John12&,,,,,,,,,,,,1000.00,08/11/2022"""

      val ((output, errors), rowNumber) = validate(data)

      errors mustBe Seq(
        ValidationError(1, 0, "whoReceivedUnauthPayment.error.format", "memberOrEmployer"),
        ValidationError(2, 1, "membersDetails.error.firstName.required", "firstName"),
        ValidationError(2, 2, "membersDetails.error.lastName.invalid", "lastName", ArraySeq("""^[a-zA-Z &`\-\'\.^]{1,35}$""")),
        ValidationError(2, 3, "genericNino.error.invalid.length", "nino"),
        ValidationError(3, 4, "doYouHoldSignedMandate.error.required", "doYouHoldSignedMandate"),
        ValidationError(3, 5, "valueOfUnauthorisedPayment.error.required", "valueOfUnauthorisedPayment"),
        ValidationError(4, 6, "schemeUnAuthPaySurchargeMember.error.required", "schemeUnAuthPaySurcharge"),
        ValidationError(5, 0, "whoReceivedUnauthPayment.error.required", "memberOrEmployer"),
        ValidationError(6, 10, "paymentNature.error.required", "natureOfPayment"),
        ValidationError(7, 11, "benefitInKindBriefDescription.error.length", "benefitDescription", ArraySeq(160)),
        ValidationError(8, 22, "whoWasTheTransferMade.error.required", "transferMadeTo"),
        ValidationError(9, 4, "doYouHoldSignedMandate.error.format", "doYouHoldSignedMandate"),
        ValidationError(9, 5, "valueOfUnauthorisedPayment.error.format", "valueOfUnauthorisedPayment"),
        ValidationError(10, 6, "schemeUnAuthPaySurchargeMember.error.format", "schemeUnAuthPaySurcharge"),
        ValidationError(11, 10, "paymentNature.error.format", "natureOfPayment"),
        ValidationError(12, 22, "whoWasTheTransferMade.error.format", "transferMadeTo"),
        ValidationError(13, 23, "schemeDetails.error.name.length", "schemeName", ArraySeq(160)),
        ValidationError(13, 23, "schemeDetails.error.ref.length", "reference", ArraySeq(160)),
        ValidationError(14, 14, "errorDescription.error.length", "errorDescription", ArraySeq(160)),
        ValidationError(15, 21, "memberTangibleMoveableProperty.error.length", "tangibleDescription", ArraySeq(160)),
        ValidationError(16, 17, "memberPaymentNatureDescription.error.length", "otherDescription", ArraySeq(160)),
        ValidationError(17, 19, "refundOfContributions.error.required", "whoReceivedRefund"),
        ValidationError(18, 19, "refundOfContributions.error.format", "whoReceivedRefund"),
        ValidationError(19, 18, "reasonForTheOverpaymentOrWriteOff.error.required", "overpaymentReason"),
        ValidationError(20, 18, "reasonForTheOverpaymentOrWriteOff.error.format", "overpaymentReason"),
        ValidationError(21, 12, "unauthorisedPaymentRecipientName.member.error.length", "courtNameOfPersonOrOrg", ArraySeq(150)),
        ValidationError(22, 12, "unauthorisedPaymentRecipientName.member.error.invalid",
          "courtNameOfPersonOrOrg", ArraySeq("""^[a-zA-Z &`\\\-\'\.^]{0,150}$"""))
      )
    }

    "return validation errors when present for the payment amount field (Member)" in {
      val data =
        s"""$header
                            member,Steven,Bloggs,AA123456A,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",,08/11/2022
                            member,Steven,Bloggs,AA123456B,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1.1.0,08/11/2022
                            member,Steven,Bloggs,AA123456C,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1000.99999,08/11/2022
                            member,Steven,Bloggs,AA123456D,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",-1000.00,08/11/2022
                            member,Steven,Bloggs,AA223456C,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",9999999999.99,08/11/2022"""


      val ((output, errors), rowNumber) = validate(data)
      errors mustBe Seq(
        ValidationError(1, 24, "paymentValueAndDate.value.error.nothingEntered", "paymentValue"),
        ValidationError(2, 24, "paymentValueAndDate.value.error.notANumber", "paymentValue"),
        ValidationError(3, 24, "paymentValueAndDate.value.error.tooManyDecimals", "paymentValue"),
        ValidationError(4, 24, "paymentValueAndDate.value.error.negative", "paymentValue", ArraySeq(0)),
        ValidationError(5, 24, "paymentValueAndDate.value.error.amountTooHigh", "paymentValue", ArraySeq(999999999.99))
      )
    }

    "return validation errors when header is not in expected format" in {
      val data =
        s"""SomeErrorHeader
                            member,Steven,Bloggs,AA123456A,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",,08/11/2022
                            member,Steven,Bloggs,AA123456B,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1.1.0,08/11/2022
                            member,Steven,Bloggs,AA123456C,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1000.99999,08/11/2022
                            member,Steven,Bloggs,AA123456D,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",-1000.00,08/11/2022
                            member,Steven,Bloggs,AA223456C,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",9999999999.99,08/11/2022"""


      val ((output, errors), rowNumber) = validate(data)
      errors mustBe Seq(
        ValidationError(0, 0, "Header is not in the expected format", ""),
        ValidationError(1, 24, "paymentValueAndDate.value.error.nothingEntered", "paymentValue"),
        ValidationError(2, 24, "paymentValueAndDate.value.error.notANumber", "paymentValue"),
        ValidationError(3, 24, "paymentValueAndDate.value.error.tooManyDecimals", "paymentValue"),
        ValidationError(4, 24, "paymentValueAndDate.value.error.negative", "paymentValue", ArraySeq(0)),
        ValidationError(5, 24, "paymentValueAndDate.value.error.amountTooHigh", "paymentValue", ArraySeq(999999999.99))
      )
    }

    "return validation errors when present for the date field, including tax year in future" in {
      val data =
        s"""$header
                            member,Steven,Bloggs,AA123456A,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1000.00,
                            member,Steven,Bloggs,AA123456B,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1000.00,/11/2022
                            member,Steven,Bloggs,AA123456C,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1000.00,08//
                            member,Steven,Bloggs,AA123456D,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1000.00,08/11/2025
                            member,Steven,Bloggs,AA123457C,YES,YES,NO,,,,TRANSFER,,,,,,,,,,,,EFRBS,"SchemeName,SchemeReference",1000.00,08/11/s"""

      val ((output, errors), rowNumber) = validate(data)

      errors mustBe Seq(
        ValidationError(1, 25, "genericDate.error.invalid.allFieldsMissing", "paymentDate", Seq("day", "month", "year")),
        ValidationError(2, 25, "The date must include a day", "paymentDate", Seq("day")),
        ValidationError(3, 25, "The date must include a month and year", "paymentDate", Seq("month", "year")),
        ValidationError(4, 25, "Date must be between 06 April 2022 and 05 April 2023", "paymentDate", Seq("day", "month", "year")),
        ValidationError(5, 25, "genericDate.error.invalid.year", "paymentDate", Seq("year"))
      )
    }

    "return validation errors when present for the Residential Address field(s) (Member)" in {
      val overMaxAddLength = "a" * 36
      val fullName = "Joe Bloggs"


      val data =
        s"""$header
              member,Joe,Bloggs,AA234567A,YES,YES,NO,,,,RESIDENTIAL,,,,,,,,,,,,,,1000.00,08/11/2022
              member,Joe,Bloggs,AA234567B,YES,YES,NO,,,,RESIDENTIAL,,,,,,,,,,"$overMaxAddLength,$overMaxAddLength,$overMaxAddLength,$overMaxAddLength,ZZ1 1ZZ,UK",,,,1000.00,08/11/2022
              member,Joe,Bloggs,AA234567C,YES,YES,NO,,,,RESIDENTIAL,,,,,,,,,,"%123Sgdfg,*&^%wfdg,25*sgsd4,!£@qfqdt,345DFG2452,UK",,,,1000.00,08/11/2022
              member,Joe,Bloggs,AA234567D,YES,YES,NO,,,,RESIDENTIAL,,,,,,,,,,"10 Other Place,Some District,Anytown,Anyplace,ZZ1 1ZZ,GIBBERISH",,,,1000.00,08/11/2022"""


      val ((output, errors), rowNumber) = validate(data)

      errors.toList mustBe Seq(
        ValidationError(1, 20, messages("address.addressLine1.error.required", fullName), "addressLine1"),
        ValidationError(1, 20, messages("address.townOrCity.error.required", fullName), "townOrCity"),
        ValidationError(1, 20, messages("address.country.error.required", fullName), "country"),
        ValidationError(2, 20, messages("address.addressLine1.error.length", fullName), "addressLine1", ArraySeq(35)),
        ValidationError(2, 20, messages("address.addressLine2.error.length", fullName), "addressLine2", ArraySeq(35)),
        ValidationError(2, 20, messages("address.townOrCity.error.length", fullName), "townOrCity", ArraySeq(35)),
        ValidationError(2, 20, messages("address.county.error.length", fullName), "county", ArraySeq(35)),
        ValidationError(3, 20, messages("address.addressLine1.error.invalid", fullName), "addressLine1", ArraySeq("^[A-Za-z0-9 &!'‘’(),./—–‐-]{1,35}$")),
        ValidationError(3, 20, messages("address.addressLine2.error.invalid", fullName), "addressLine2", ArraySeq("^[A-Za-z0-9 &!'‘’(),./—–‐-]{1,35}$")),
        ValidationError(3, 20, messages("address.townOrCity.error.invalid", fullName), "townOrCity", ArraySeq("^[A-Za-z0-9 &!'‘’(),./—–‐-]{1,35}$")),
        ValidationError(3, 20, messages("address.county.error.invalid", fullName), "county", ArraySeq("^[A-Za-z0-9 &!'‘’(),./—–‐-]{1,35}$")),
        ValidationError(3, 20, messages("address.postCode.error.invalid", fullName), "postCode"),
        ValidationError(4, 20, messages("address.country.error.required", fullName), "country")
      )
    }

    "return validation errors when present for the Company details fields (Employer)" in {
      val data =
        s"""$header
                            employer,,,,,,,,,"$validAddress",LOANS,,,,,10.00,20.57,,,,,,,,1000.00,08/11/2022
                            employer,,,,,,,$moreThanMax,123456789,"$validAddress",LOANS,,,,,10.00,20.57,,,,,,,,1000.00,08/11/2022
                            employer,,,,,,,{invalid},AB12£212,"$validAddress",LOANS,,,,,10.00,20.57,,,,,,,,1000.00,08/11/2022
                            employer,,,,,,,Company Name,12,"$validAddress",LOANS,,,,,10.00,20.57,,,,,,,,1000.00,08/11/2022"""

      val invalidRegex = """^[a-zA-Z0-9À-ÿ !#$%&'‘’"“”«»()*+,./:;=?@\\\[\]|~£€¥\—–‐_^`-]{1,160}$"""

      val ((output, errors), rowNumber) = validate(data)

      errors mustBe Seq(
        ValidationError(1, 7, "companyDetails.companyName.error.required", "companyName"),
        ValidationError(1, 8, "companyDetails.companyNumber.error.required", "companyNumber"),
        ValidationError(2, 7, "companyDetails.companyName.error.length", "companyName", ArraySeq(160)),
        ValidationError(2, 8, "companyDetails.companyNumber.error.length", "companyNumber", ArraySeq(8)),
        ValidationError(3, 7, "companyDetails.companyName.error.invalidCharacters", "companyName", ArraySeq(invalidRegex)),
        ValidationError(3, 8, "companyDetails.companyNumber.error.invalidCharacters", "companyNumber", ArraySeq("^[A-Za-z0-9 -]{7,8}$")),
        ValidationError(4, 8, "companyDetails.companyNumber.error.length", "companyNumber", ArraySeq(6))
      )
    }

    "return validation errors when present for the Payment Nature fields (Employer)" in {
      val data =
        s"""$header
                            $commonUaEmployer,"$validAddress",,,,,,10.00,20.57,,,,,,,,1000.00,08/11/2022
                            $commonUaEmployer,"$validAddress",Loansdfass,,,,,10.00,20.57,,,,,,,,1000.00,08/11/2022
                            $commonUaEmployer,"$validAddress",TANGIBLE,,,,,,,,,,,$moreThanMax,,,1000.00,08/11/2022
                            $commonUaEmployer,"$validAddress",COURT,,$moreThanMax,,,,,,,,,,,,1000.00,08/11/2022
                            $commonUaEmployer,"$validAddress",COURT,,Organisation£# Name,,,,,,,,,,,,1000.00,08/11/2022
                            $commonUaEmployer,"$validAddress",OTHER,,,,,,,$moreThanMax,,,,,,,1000.00,08/11/2022
                            $commonUaEmployer,"$validAddress",TANGIBLE,,,,,,,,,,,$moreThanMaxAndSpecialChar,,,1000.00,08/11/2022"""

      val ((output, errors), rowNumber) = validate(data)

      errors mustBe Seq(
        ValidationError(1, 10, "paymentNature.error.required", "natureOfPayment"),
        ValidationError(2, 10, "paymentNature.error.format", "natureOfPayment"),
        ValidationError(3, 21, "employerTangibleMoveableProperty.error.length", "tangibleDescription", ArraySeq(160)),
        ValidationError(4, 12, "unauthorisedPaymentRecipientName.employer.error.length", "courtNameOfPersonOrOrg", ArraySeq(160)),
        ValidationError(5, 12, "unauthorisedPaymentRecipientName.employer.error.invalid", "courtNameOfPersonOrOrg",
          ArraySeq("""^[a-zA-Z &`\'\.^\\]{0,160}$""")),
        ValidationError(6, 17, "employerPaymentNatureDescription.error.length", "otherDescription", ArraySeq(160)),
        ValidationError(7, 21, "description.error.invalid", "tangibleDescription")
      )
    }

  }

}
