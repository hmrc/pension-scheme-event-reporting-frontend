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

package helpers.fileUpload

import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event6}
import models.fileUpload.FileUploadHeaders._
import services.fileUpload.ValidationError

object FileUploadGenericErrorReporter {

  import models.fileUpload.FileUploadHeaders.MemberDetailsFieldNames._

  case class ColumnAndError(columnName: String, errorDescription: String)

  type ErrorReport = Seq[String]
  type ColumnAndErrorMap = Map[String, String]

  private val commonColumnAndErrorMessageMap = Map(
    firstName -> "fileUpload.memberDetails.generic.error.firstName",
    lastName -> "fileUpload.memberDetails.generic.error.lastName",
    nino -> "fileUpload.memberDetails.generic.error.nino"
  )


  //noinspection ScalaStyle
  private def eventHeader(eventType: EventType) = {
    eventType match {
      case Event1 =>
        commonColumnAndErrorMessageMap ++
          Map(
            Event1FieldNames.memberOrEmployer -> "fileUpload.memberOrEmployer.generic.error",
            Event1FieldNames.doYouHoldSignedMandate -> "fileUpload.doYouHoldSignedMandate.generic.error",
            Event1FieldNames.valueOfUnauthorisedPayment -> "fileUpload.valueOfUnauthorisedPayment.generic.error",
            Event1FieldNames.schemeUnAuthPaySurcharge -> "fileUpload.schemeUnAuthPaySurcharge.generic.error",
            Event1FieldNames.natureOfPayment -> "fileUpload.natureOfPayment.generic.error",
            Event1FieldNames.benefitDescription -> "fileUpload.benefitDescription.generic.error",
            Event1FieldNames.transferMadeTo -> "fileUpload.transferMadeTo.generic.error",
            Event1FieldNames.schemeDetails -> "fileUpload.schemeDetails.generic.error",
            Event1FieldNames.schemeName -> "fileUpload.schemeName.generic.error",
            Event1FieldNames.schemeReference -> "fileUpload.schemeReference.generic.error",
            Event1FieldNames.overpaymentReason -> "fileUpload.overpaymentReason.generic.error",
            Event1FieldNames.addressLine1 -> "fileUpload.addressLine1.generic.error",
            Event1FieldNames.addressLine2 -> "fileUpload.addressLine2.generic.error",
            Event1FieldNames.addressLine3 -> "fileUpload.addressLine3.generic.error",
            Event1FieldNames.addressLine4 -> "fileUpload.addressLine4.generic.error",
            Event1FieldNames.postCode -> "fileUpload.postCode.generic.error",
            Event1FieldNames.country -> "fileUpload.country.generic.error",
            Event1FieldNames.tangibleDescription -> "fileUpload.tangibleDescription.generic.error",
            Event1FieldNames.courtNameOfPersonOrOrg -> "fileUpload.courtNameOfPersonOrOrg.generic.error",
            Event1FieldNames.otherDescription -> "fileUpload.otherDescription.generic.error",
            Event1FieldNames.errorDescription -> "fileUpload.errorDescription.generic.error",
            Event1FieldNames.earlyDescription -> "fileUpload.earlyDescription.generic.error",
            Event1FieldNames.companyOrOrgName -> "fileUpload.companyOrOrgName.generic.error",
            Event1FieldNames.companyNo -> "fileUpload.companyNo.generic.error",
            Event1FieldNames.loanAmount -> "fileUpload.loanAmount.generic.error",
            Event1FieldNames.valueOfFund -> "fileUpload.valueOfFund.generic.error",
            Event1FieldNames.paymentAmount -> "fileUpload.paymentAmount.generic.error",
            Event1FieldNames.paymentDate -> "fileUpload.paymentDate.generic.error"
          )
      case Event6 =>
        commonColumnAndErrorMessageMap ++
          Map(
            Event6FieldNames.typeOfProtection -> "fileUpload.typeOfProtection.generic.error",
            Event6FieldNames.typeOfProtectionReference -> "fileUpload.typeOfProtectionReference.generic.error",
            Event6FieldNames.lumpSumAmount -> "fileUpload.lumpSumAmount.generic.error",
            Event6FieldNames.lumpSumDate -> "fileUpload.lumpSumDate.generic.error"
          )
      case Event22 =>
        commonColumnAndErrorMessageMap ++
          Map(
            Event22FieldNames.taxYear -> "fileUpload.taxYear.generic.error",
            Event22FieldNames.totalAmounts -> "fileUpload.totalAmounts.generic.error"
          )
      case _ =>
        commonColumnAndErrorMessageMap ++
          Map(
            Event23FieldNames.taxYear -> "fileUpload.taxYear.generic.error",
            Event23FieldNames.totalAmounts -> "fileUpload.totalAmounts.generic.error"
          )
    }

  }

  private def getColumnsAndErrorMap(eventType: EventType): ColumnAndErrorMap = eventType match {
    case EventType.Event1 => eventHeader(eventType)
    case EventType.Event6 => eventHeader(eventType)
    case EventType.Event22 => eventHeader(eventType)
    case EventType.Event23 => eventHeader(eventType)
    case _ => throw new RuntimeException("Invalid event type")
  }

  def generateGenericErrorReport(errors: Seq[ValidationError], eventType: EventType): ErrorReport = {
    val eventTypeHeaderMap = getColumnsAndErrorMap(eventType)
    val columns = errors.map(_.columnName).intersect(eventTypeHeaderMap.keySet.toSeq)
    columns.map(col => eventTypeHeaderMap.apply(col))
  }

}
