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

package models.fileUpload

object FileUploadHeaders {

  val valueFormField = "value"

  object MemberDetailsFieldNames {
    val firstName = "firstName"
    val lastName = "lastName"
    val nino = "nino"
  }

  object Event1FieldNames {
    val memberOrEmployer: String = "memberOrEmployer"
    val doYouHoldSignedMandate: String = "doYouHoldSignedMandate"
    val valueOfUnauthorisedPayment: String = "valueOfUnauthorisedPayment"
    val schemeUnAuthPaySurcharge: String = "schemeUnAuthPaySurcharge"
    val natureOfPayment: String = "natureOfPayment"
    val benefitDescription: String = "benefitDescription"
    val transferMadeTo: String = "transferMadeTo"
    val schemeDetails: String = "schemeDetails"
    val schemeName: String = "schemeName"
    val schemeReference: String = "reference"
    val whoReceivedRefund: String = "whoReceivedRefund"
    val overpaymentReason: String = "overpaymentReason"
    val addressLine1: String = "addressLine1"
    val addressLine2: String = "addressLine2"
    val townOrCity: String = "townOrCity"
    val county: String = "county"
    val postCode: String = "postCode"
    val country: String = "country"
    val tangibleDescription: String = "tangibleDescription"
    val courtNameOfPersonOrOrg: String = "courtNameOfPersonOrOrg"
    val otherDescription: String = "otherDescription"
    val errorDescription: String = "errorDescription"
    val earlyDescription: String = "earlyDescription"
    val companyOrOrgName: String = "companyName"
    val companyNo: String = "companyNumber"
    val loanAmount: String = "loanAmount"
    val valueOfFund: String = "fundValue"
    val paymentAmount: String = "paymentValue"
    val paymentDate: String = "paymentDate"
    val dateOfEventDay: String = "paymentDate.day"
    val dateOfEventMonth: String = "paymentDate.month"
    val dateOfEventYear: String = "paymentDate.year"
  }

  object Event6FieldNames {
    val typeOfProtection: String = "typeOfProtection"
    val typeOfProtectionReference: String = "typeOfProtectionReference"
    val lumpSumAmount: String = "amountCrystallised"
    val lumpSumDate: String = "crystallisedDate"
    val dateOfEventDay: String = "crystallisedDate.day"
    val dateOfEventMonth: String = "crystallisedDate.month"
    val dateOfEventYear: String = "crystallisedDate.year"
  }

  object Event22FieldNames {
    val taxYear: String = "taxYear"
    val totalAmounts: String = "totalAmounts"
  }

  object Event23FieldNames {
    val taxYear: String = "taxYear"
    val totalAmounts: String = "totalAmounts"
  }

  object Event24FieldNames {
    val crystallisedDate = "crystallisedDate"
    val dateOfEventDay = "crystallisedDate.day"
    val dateOfEventMonth = "crystallisedDate.month"
    val dateOfEventYear = "crystallisedDate.year"
    val bceType = "bceType"
    val totalAmount = "totalAmount"
    val validProtection = "validProtection"
    val protectionTypeGroup1 = "protectionTypeGroup1"
    val nonResidenceReference = "nonResidenceEnhancement"
    val pensionCreditsReference = "pensionCreditsPreCRE"
    val preCommencementReference = "preCommencement"
    val overseasReference = "recognisedOverseasPSTE"
    val schemeSpecific = "schemeSpecific"
    val protectionTypeGroup2 = "protectionTypeGroup2"
    val protectionTypeGroup2Reference = "protectionTypeGroup2Reference"
    val overAllowance = "overAllowance"
    val overAllowanceAndDeathBenefit = "overAllowanceAndDeathBenefit"
    val marginalRate = "marginalRate"
    val employerPayeRef = "employerPayeRef"
  }
}
