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

package services.fileUpload

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.toFoldableOps
import com.google.inject.Inject
import config.FrontendAppConfig
import forms.address.ManualAddressFormProvider
import forms.common.MembersDetailsFormProvider
import forms.event1.employer.{CompanyDetailsFormProvider, LoanDetailsFormProvider, PaymentNatureFormProvider => EmployerPaymentNatureFormProvider, UnauthorisedPaymentRecipientNameFormProvider => EmployerUnauthorisedPaymentRecipientNameFormProvider}
import forms.event1.member._
import forms.event1.{PaymentNatureFormProvider => MemberPaymentNatureFormProvider, _}
import models.address.Address
import models.common.MembersDetails
import models.enumeration.EventType
import models.enumeration.EventType.Event1
import models.event1.employer.{CompanyDetails, LoanDetails, PaymentNature => EmployerPaymentNature}
import models.event1.member.{ReasonForTheOverpaymentOrWriteOff, RefundOfContributions, SchemeDetails, WhoWasTheTransferMade}
import models.event1.{PaymentDetails, WhoReceivedUnauthPayment, PaymentNature => MemberPaymentNature}
import models.fileUpload.FileUploadHeaders.Event1FieldNames._
import models.fileUpload.FileUploadHeaders.{Event1FieldNames, valueFormField}
import pages.common.MembersDetailsPage
import pages.event1.member.{BenefitInKindBriefDescriptionPage, WhoWasTheTransferMadePage, PaymentNaturePage => MemberPaymentNaturePage}
import pages.event1._
import play.api.data.Form
import play.api.i18n.Messages
import services.fileUpload.Validator.Result

class Event1Validator @Inject()(
                                 whoReceivedUnauthPaymentFormProvider: WhoReceivedUnauthPaymentFormProvider,
                                 membersDetailsFormProvider: MembersDetailsFormProvider,
                                 doYouHoldSignedMandateFormProvider: DoYouHoldSignedMandateFormProvider,
                                 paymentValueAndDateFormProvider: PaymentValueAndDateFormProvider,
                                 valueOfUnauthorisedPaymentFormProvider: ValueOfUnauthorisedPaymentFormProvider,
                                 schemeUnAuthPaySurchargeMemberFormProvider: SchemeUnAuthPaySurchargeMemberFormProvider,
                                 memberPaymentNatureFormProvider: MemberPaymentNatureFormProvider,
                                 benefitInKindBriefDescriptionFormProvider: BenefitInKindBriefDescriptionFormProvider,
                                 whoWasTheTransferMadeFormProvider: WhoWasTheTransferMadeFormProvider,
                                 schemeDetailsFormProvider: SchemeDetailsFormProvider,
                                 errorDescriptionFormProvider: ErrorDescriptionFormProvider,
                                 benefitsPaidEarlyFormProvider: BenefitsPaidEarlyFormProvider,
                                 refundOfContributionsFormProvider: RefundOfContributionsFormProvider,
                                 reasonForTheOverpaymentOrWriteOffFormProvider: ReasonForTheOverpaymentOrWriteOffFormProvider,
                                 manualAddressFormProvider: ManualAddressFormProvider,
                                 memberTangibleMoveablePropertyFormProvider: MemberTangibleMoveablePropertyFormProvider,
                                 memberUnauthorisedPaymentRecipientNameFormProvider: UnauthorisedPaymentRecipientNameFormProvider,
                                 memberPaymentNatureDescriptionFormProvider: MemberPaymentNatureDescriptionFormProvider,
                                 companyDetailsFormProvider: CompanyDetailsFormProvider,
                                 employerPaymentNatureFormProvider: EmployerPaymentNatureFormProvider,
                                 loanDetailsFormProvider: LoanDetailsFormProvider,
                                 employerTangibleMoveablePropertyFormProvider: EmployerTangibleMoveablePropertyFormProvider,
                                 employerUnauthorisedPaymentRecipientNameFormProvider: EmployerUnauthorisedPaymentRecipientNameFormProvider,
                                 employerPaymentNatureDescriptionFormProvider: EmployerPaymentNatureDescriptionFormProvider,
                                 config: FrontendAppConfig
                               ) extends Validator {

  override val eventType: EventType = EventType.Event1

  override protected def validHeader: String = config.validEvent1Header

  private val fieldNoMemberOrEmployer = 0
  override val fieldNoFirstName = 1
  override val fieldNoLastName = 2
  override val fieldNoNino = 3
  private val fieldNoDoYouHoldSignedMandate = 4
  private val fieldNoValueOfUnauthorisedPayment = 5
  private val fieldNoSchemeUnAuthPaySurcharge = 6
  private val fieldNoCompanyOrOrgName = 7
  private val fieldNoCompanyNo = 8
  private val fieldNoCompanyAddress = 9
  private val fieldNoNatureOfPayment = 10
  private val fieldNoBenefitDescription = 11
  private val fieldNoCourtNameOfPersonOrOrg = 12
  private val fieldNoEarlyDescription = 13
  private val fieldNoErrorDescription = 14
  private val fieldNoLoanAmount = 15
  private val fieldNoValueOfFund = 16
  private val fieldNoOtherDescription = 17
  private val fieldNoOverpaymentReason = 18
  private val fieldNoWhoReceivedRefund = 19
  private val fieldNoResidentialAddress = 20
  private val fieldNoTangibleDescription = 21
  private val fieldNoTransferMadeTo = 22
  private val fieldNoTransferSchemeDetails = 23
  private val fieldNoPaymentAmount = 24
  private val fieldNoPaymentDate = 25

  private val mapPaymentNatureMember: Map[String, String] = {
    Map(
      "Benefit" -> "benefitInKind",
      "Transfer" -> "transferToNonRegPensionScheme",
      "Error" -> "errorCalcTaxFreeLumpSums",
      "Early" -> "benefitsPaidEarly",
      "Refund" -> "refundOfContributions",
      "Overpayment" -> "overpaymentOrWriteOff",
      "Residential" -> "residentialPropertyHeld",
      "Tangible" -> "tangibleMoveablePropertyHeld",
      "Court" -> "courtOrConfiscationOrder",
      "Other" -> "memberOther"
    )
  }

  private val mapOverpayment: Map[String, String] = {
    Map(
      "DEATH OF MEMBER" -> "deathOfMember",
      "DEATH OF DEPENDENT" -> "deathOfDependent",
      "NO LONGER QUALIFIED" -> "dependentNoLongerQualifiedForPension",
      "OTHER" -> "other"
    )
  }

  private val mapRefund: Map[String, String] = {
    Map(
      "WIDOW/ORPHAN" -> "widowOrOrphan",
      "OTHER" -> "other"
    )
  }

  private val mapPaymentNatureEmployer: Map[String, String] = {
    Map(
      "Loans" -> "loansExceeding50PercentOfFundValue",
      "Residential" -> "residentialProperty",
      "Tangible" -> "tangibleMoveableProperty",
      "Court" -> "courtOrder",
      "Other" -> "employerOther"
    )
  }

  private def toBoolean(s: String): String = if (s == "YES") "true" else "false"

  private def whoReceivedUnauthPaymentValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], WhoReceivedUnauthPayment] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoMemberOrEmployer), memberOrEmployer, fieldNoMemberOrEmployer)
    )
    val form: Form[WhoReceivedUnauthPayment] = whoReceivedUnauthPaymentFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def doYouHoldSignedMandateValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Boolean] = {

    val mappedBoolean = toBoolean(chargeFields(fieldNoDoYouHoldSignedMandate))

    val fields = Seq(
      Field(valueFormField, mappedBoolean, doYouHoldSignedMandate, fieldNoDoYouHoldSignedMandate)
    )
    val form: Form[Boolean] = doYouHoldSignedMandateFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def valueOfUnauthorisedPaymentValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Boolean] = {

    val mappedBoolean = toBoolean(chargeFields(fieldNoValueOfUnauthorisedPayment))

    val fields = Seq(
      Field(valueFormField, mappedBoolean, valueOfUnauthorisedPayment, fieldNoValueOfUnauthorisedPayment)
    )
    val form: Form[Boolean] = valueOfUnauthorisedPaymentFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def schemeUnAuthPaySurchargeMemberValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Boolean] = {

    val mappedBoolean = toBoolean(chargeFields(fieldNoSchemeUnAuthPaySurcharge))

    val fields = Seq(
      Field(valueFormField, mappedBoolean, schemeUnAuthPaySurcharge, fieldNoSchemeUnAuthPaySurcharge)
    )
    val form: Form[Boolean] = schemeUnAuthPaySurchargeMemberFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def memberPaymentNatureValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], MemberPaymentNature] = {

    val mappedNatureOfPayment = mapPaymentNatureMember.applyOrElse[String, String](chargeFields(fieldNoNatureOfPayment),
      (_: String) => "Nature of the payment is not found or doesn't exist")

    val fields = Seq(
      Field(valueFormField, mappedNatureOfPayment, natureOfPayment, fieldNoNatureOfPayment)
    )
    val form: Form[MemberPaymentNature] = memberPaymentNatureFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def test[A](index: Int, chargeFields: Seq[String], formGeneric: Form[A]): Validated[Seq[ValidationError], A] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoBenefitDescription), benefitDescription, fieldNoBenefitDescription)
    )
    val form: Form[A] = formGeneric.ap()
    formGeneric.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def benefitInKindBriefDescriptionValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Option[String]] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoBenefitDescription), benefitDescription, fieldNoBenefitDescription)
    )
    val form: Form[Option[String]] = benefitInKindBriefDescriptionFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def whoWasTheTransferMadeValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], WhoWasTheTransferMade] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTransferMadeTo), transferMadeTo, fieldNoTransferMadeTo)
    )
    val form: Form[WhoWasTheTransferMade] = whoWasTheTransferMadeFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  //TODO: Come back to the splitting of this
  private def schemeDetailsValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], SchemeDetails] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTransferSchemeDetails), schemeDetails, fieldNoTransferSchemeDetails)
    )
    val form: Form[SchemeDetails] = schemeDetailsFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def errorDescriptionValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Option[String]] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoErrorDescription), errorDescription, fieldNoErrorDescription)
    )
    val form: Form[Option[String]] = errorDescriptionFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def benefitsPaidEarlyValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], String] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoEarlyDescription), earlyDescription, fieldNoEarlyDescription)
    )
    val form: Form[String] = benefitsPaidEarlyFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def refundOfContributionsValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], RefundOfContributions] = {

    val mappedRefundReason = mapRefund.applyOrElse[String, String](chargeFields(fieldNoWhoReceivedRefund),
      (_: String) => "Reason of the refund is not found or doesn't exist")

    val fields = Seq(
      Field(valueFormField, mappedRefundReason, whoReceivedRefund, fieldNoWhoReceivedRefund)
    )
    val form: Form[RefundOfContributions] = refundOfContributionsFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def reasonForTheOverpaymentOrWriteOffValidation(index: Int,
                                                          chargeFields: Seq[String]): Validated[Seq[ValidationError], ReasonForTheOverpaymentOrWriteOff] = {

    val mappedOverpayment = mapOverpayment.applyOrElse[String, String](chargeFields(fieldNoOverpaymentReason),
      (_: String) => "Reason for the overpayment is not found or doesn't exist")

    val fields = Seq(
      Field(valueFormField, mappedOverpayment, overpaymentReason, fieldNoOverpaymentReason)
    )
    val form: Form[ReasonForTheOverpaymentOrWriteOff] = reasonForTheOverpaymentOrWriteOffFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def memberTangibleMoveablePropertyValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Option[String]] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTangibleDescription), tangibleDescription, fieldNoTangibleDescription)
    )
    val form: Form[Option[String]] = memberTangibleMoveablePropertyFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def unauthorisedPaymentRecipientNameValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Option[String]] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoCourtNameOfPersonOrOrg), courtNameOfPersonOrOrg, fieldNoCourtNameOfPersonOrOrg)
    )
    val form: Form[Option[String]] = memberUnauthorisedPaymentRecipientNameFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def memberPaymentNatureDescriptionValidation(index: Int,
                                                       chargeFields: Seq[String]): Validated[Seq[ValidationError], Option[String]] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoOtherDescription), otherDescription, fieldNoOtherDescription)
    )
    val form: Form[Option[String]] = memberPaymentNatureDescriptionFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def companyDetailsValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], CompanyDetails] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoCompanyOrOrgName), companyOrOrgName, fieldNoCompanyOrOrgName),
      Field(valueFormField, chargeFields(fieldNoCompanyNo), companyNo, fieldNoCompanyNo)
    )
    val form: Form[CompanyDetails] = companyDetailsFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def employerPaymentNatureValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], EmployerPaymentNature] = {
    val mappedNatureOfPayment = mapPaymentNatureEmployer.applyOrElse[String, String](chargeFields(fieldNoNatureOfPayment),
      (_: String) => "Nature of the payment is not found or doesn't exist")

    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoNatureOfPayment), companyOrOrgName, fieldNoNatureOfPayment)
    )
    val form: Form[EmployerPaymentNature] = employerPaymentNatureFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def loanDetailsValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], LoanDetails] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoLoanAmount), loanAmount, fieldNoLoanAmount),
      Field(valueFormField, chargeFields(fieldNoValueOfFund), valueOfFund, fieldNoValueOfFund)
    )
    val form: Form[LoanDetails] = loanDetailsFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def employerTangibleMoveablePropertyValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Option[String]] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTangibleDescription), tangibleDescription, fieldNoTangibleDescription)
    )
    val form: Form[Option[String]] = employerTangibleMoveablePropertyFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def employerUnauthorisedPaymentRecipientValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Option[String]] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoCourtNameOfPersonOrOrg), courtNameOfPersonOrOrg, fieldNoCourtNameOfPersonOrOrg)
    )
    val form: Form[Option[String]] = employerUnauthorisedPaymentRecipientNameFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def employerPaymentNatureDescriptionValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Option[String]] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoOtherDescription), otherDescription, fieldNoOtherDescription)
    )
    val form: Form[Option[String]] = employerPaymentNatureDescriptionFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def addressValidation(index: Int, chargeFields: Seq[String], fieldNumber: Int): Validated[Seq[ValidationError], Address] = {
    val parsedAddress = splitAddress(chargeFields(fieldNumber))
    val fields = Seq(
      Field(addressLine1, parsedAddress.addressLine1, addressLine1, fieldNumber, Some(Event1FieldNames.addressLine1)),
      Field(addressLine2, parsedAddress.addressLine2, addressLine2, fieldNumber, Some(Event1FieldNames.addressLine2)),
      Field(addressLine3, parsedAddress.addressLine3, addressLine3, fieldNumber, Some(Event1FieldNames.addressLine3)),
      Field(addressLine4, parsedAddress.addressLine4, addressLine4, fieldNumber, Some(Event1FieldNames.addressLine4)),
      Field(postCode, parsedAddress.postCode, postCode, fieldNumber, Some(Event1FieldNames.postCode)),
      Field(country, parsedAddress.country, country, fieldNumber, Some(Event1FieldNames.country))
    )
    val form: Form[Address] = manualAddressFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def paymentValueAndDateValidation(index: Int,
                                            chargeFields: Seq[String],
                                            taxYear: Int)
                                           (implicit messages: Messages): Validated[Seq[ValidationError], PaymentDetails] = {

    val parsedDate = splitDayMonthYear(chargeFields(fieldNoPaymentDate))

    val fields = Seq(
      Field(paymentAmount, chargeFields(fieldNoPaymentAmount), paymentAmount, fieldNoPaymentAmount),
      Field(dateOfEventDay, parsedDate.day, paymentDate, fieldNoPaymentDate, Some(Event1FieldNames.paymentDate)),
      Field(dateOfEventMonth, parsedDate.month, paymentDate, fieldNoPaymentDate, Some(Event1FieldNames.paymentDate)),
      Field(dateOfEventYear, parsedDate.year, paymentDate, fieldNoPaymentDate, Some(Event1FieldNames.paymentDate))
    )

    val form: Form[PaymentDetails] = paymentValueAndDateFormProvider(taxYear)

    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  //noinspection ScalaStyle
  override protected def validateFields(index: Int,
                                        columns: Seq[String],
                                        taxYear: Int,
                                        members: Seq[MembersDetails])
                                       (implicit messages: Messages): Result = {

    columns.head match {
      case "member" =>
        val a = resultFromFormValidationResult[WhoReceivedUnauthPayment](
          whoReceivedUnauthPaymentValidation(index, columns), createCommitItem(index, WhoReceivedUnauthPaymentPage.apply(_))
        )

        val b = resultFromFormValidationResultForMembersDetails(
          memberDetailsValidation(index, columns, membersDetailsFormProvider(Event1, index)),
          createCommitItem(index, MembersDetailsPage.apply(Event1, _)),
          members
        )

        val c = resultFromFormValidationResult[Boolean](
          doYouHoldSignedMandateValidation(index, columns), createCommitItem(index, DoYouHoldSignedMandatePage.apply(_))
        )

        val d = resultFromFormValidationResult[Boolean](
          valueOfUnauthorisedPaymentValidation(index, columns), createCommitItem(index, ValueOfUnauthorisedPaymentPage.apply(_))
        )

        val e = resultFromFormValidationResult[Boolean](
          schemeUnAuthPaySurchargeMemberValidation(index, columns), createCommitItem(index, SchemeUnAuthPaySurchargeMemberPage.apply(_))
        )

        val k = resultFromFormValidationResult[MemberPaymentNature](
          memberPaymentNatureValidation(index, columns), createCommitItem(index, MemberPaymentNaturePage.apply(_))
        )

        val paymentNatureNextStep = columns(10) match {
          case "Benefit" =>
            resultFromFormValidationResult[Option[String]](
              benefitInKindBriefDescriptionValidation(index, columns), createCommitItem(index, BenefitInKindBriefDescriptionPage.apply(_)))
//          case "Transfer" =>
//            resultFromFormValidationResult[WhoWasTheTransferMade](
//              whoWasTheTransferMadeValidation(index, columns), createCommitItem(index, WhoWasTheTransferMadePage.apply(_)))
//          case "Error" =>
//          case "Early" =>
//          case "Refund" =>
//          case "Overpayment" =>
//          case "Residential" =>
//          case "Tangible" =>
//          case "Court" =>
//          case "Other" =>
//          case _ => throw new RuntimeException("Nature of payment not found or doesn't exist")

        }

        val y = resultFromFormValidationResult[PaymentDetails](
          paymentValueAndDateValidation(index, columns, taxYear), createCommitItem(index, PaymentValueAndDatePage.apply(_))
        )


        Seq(a, b, c, d, e, k, paymentNatureNextStep, y).combineAll

      //      case "employer" =>
      //        val a = resultFromFormValidationResult[WhoReceivedUnauthPayment](
      //          whoReceivedUnauthPaymentValidation(index, columns), createCommitItem(index, WhoReceivedUnauthPaymentPage.apply(_))
      //        )
      //
      //        val b = resultFromFormValidationResultForMembersDetails(
      //          memberDetailsValidation(index, columns, membersDetailsFormProvider(Event1, index)),
      //          createCommitItem(index, MembersDetailsPage.apply(Event1, _)),
      //          members
      //        )
      //
      //        val c = resultFromFormValidationResult[Boolean](
      //          doYouHoldSignedMandateValidation(index, columns), createCommitItem(index, DoYouHoldSignedMandatePage.apply(_))
      //        )
      //
      //        val d = resultFromFormValidationResult[Boolean](
      //          valueOfUnauthorisedPaymentValidation(index, columns), createCommitItem(index, ValueOfUnauthorisedPaymentPage.apply(_))
      //        )
      //
      //        val e = resultFromFormValidationResult[Boolean](
      //          schemeUnAuthPaySurchargeMemberValidation(index, columns), createCommitItem(index, SchemeUnAuthPaySurchargeMemberPage.apply(_))
      //        )
      //
      //        Seq(a, b, c, d, e).combineAll
      case _ => throw new RuntimeException("Something went wrong")
    }
  }
}
