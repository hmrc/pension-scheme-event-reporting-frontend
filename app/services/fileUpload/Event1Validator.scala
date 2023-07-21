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

//noinspection ScalaStyle

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
import models.enumeration.AddressJourneyType.{Event1EmployerAddressJourney, Event1EmployerPropertyAddressJourney, Event1MemberPropertyAddressJourney}
import models.enumeration.EventType
import models.enumeration.EventType.Event1
import models.event1.employer.{CompanyDetails, LoanDetails, PaymentNature => EmployerPaymentNature}
import models.event1.member.{ReasonForTheOverpaymentOrWriteOff, RefundOfContributions, SchemeDetails, WhoWasTheTransferMade}
import models.event1.{PaymentDetails, WhoReceivedUnauthPayment, PaymentNature => MemberPaymentNature}
import models.fileUpload.FileUploadHeaders.Event1FieldNames._
import models.fileUpload.FileUploadHeaders.{Event1FieldNames, valueFormField}
import pages.address.ManualAddressPage
import pages.common.MembersDetailsPage
import pages.event1._
import pages.event1.employer.{CompanyDetailsPage, EmployerPaymentNatureDescriptionPage, EmployerTangibleMoveablePropertyPage, LoanDetailsPage, PaymentNaturePage => EmployerPaymentNaturePage, UnauthorisedPaymentRecipientNamePage => EmployerUnauthorisedPaymentRecipientNamePage}
import pages.event1.member.{BenefitInKindBriefDescriptionPage, BenefitsPaidEarlyPage, ErrorDescriptionPage, MemberPaymentNatureDescriptionPage, MemberTangibleMoveablePropertyPage, ReasonForTheOverpaymentOrWriteOffPage, RefundOfContributionsPage, SchemeDetailsPage, UnauthorisedPaymentRecipientNamePage, WhoWasTheTransferMadePage, PaymentNaturePage => MemberPaymentNaturePage}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.JsString
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
  override protected val fieldNoFirstName = 1
  override protected val fieldNoLastName = 2
  override protected val fieldNoNino = 3
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

  private val mapPaymentNatureEmployer: Map[String, String] = {
    Map(
      "Loans" -> "loansExceeding50PercentOfFundValue",
      "Residential" -> "residentialProperty",
      "Tangible" -> "tangibleMoveableProperty",
      "Court" -> "courtOrder",
      "Other" -> "employerOther"
    )
  }

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

  private val mapTransferMadeTo: Map[String, String] = {
    Map(
      "EMPLOYER" -> "anEmployerFinanced",
      "NON RECOGNISED PENSION SCHEME" -> "nonRecognisedScheme",
      "OTHER" -> "other"
    )
  }

  private def toBoolean(s: String): String = s match {
    case "YES" => "true"
    case "NO" => "false"
    case _ => s
  }

  private case class FieldInfoForValidation[A](fieldNum: Int, description: String, form: Form[A])

  private def genericBooleanFieldValidation[Boolean](index: Int, chargeFields: Seq[String], fieldInfoForValidation: FieldInfoForValidation[Boolean]): Validated[Seq[ValidationError], Boolean] = {
    val mappedBoolean = toBoolean(chargeFields(fieldInfoForValidation.fieldNum))
    val fields = Seq(Field(valueFormField, mappedBoolean, fieldInfoForValidation.description, fieldInfoForValidation.fieldNum))
    fieldInfoForValidation.form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def genericPaymentNatureFieldValidation[A](index: Int,
                                                     chargeFields: Seq[String],
                                                     fieldInfoForValidation: FieldInfoForValidation[A],
                                                     mapPaymentNature: Map[String, String]): Validated[Seq[ValidationError], A] = {

    val mappedNatureOfPayment = mapPaymentNature.applyOrElse[String, String](chargeFields(fieldInfoForValidation.fieldNum),
      (_: String) => "Nature of the payment is not found or doesn't exist")
    val fields = Seq(Field(valueFormField, mappedNatureOfPayment, fieldInfoForValidation.description, fieldInfoForValidation.fieldNum))
    fieldInfoForValidation.form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def genericFieldValidation[A](index: Int, chargeFields: Seq[String], fieldInfoForValidation: FieldInfoForValidation[A]): Validated[Seq[ValidationError], A] = {
    val fields = Seq(Field(valueFormField, chargeFields(fieldInfoForValidation.fieldNum), fieldInfoForValidation.description, fieldInfoForValidation.fieldNum))
    fieldInfoForValidation.form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def whoWasTheTransferMadeValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], WhoWasTheTransferMade] = {

    val mappedTransferMadeTo = mapTransferMadeTo.applyOrElse[String, String](chargeFields(fieldNoTransferMadeTo),
      (_: String) => "Option is not found or doesn't exist")
    val fields = Seq(
      Field(valueFormField, mappedTransferMadeTo, transferMadeTo, fieldNoTransferMadeTo)
    )
    val form: Form[WhoWasTheTransferMade] = whoWasTheTransferMadeFormProvider()
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

  private def loanDetailsValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], LoanDetails] = {
    val fields = Seq(
      Field(loanAmount, chargeFields(fieldNoLoanAmount), loanAmount, fieldNoLoanAmount),
      Field(valueOfFund, chargeFields(fieldNoValueOfFund), valueOfFund, fieldNoValueOfFund)
    )
    val form: Form[LoanDetails] = loanDetailsFormProvider()
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

  private def schemeDetailsValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], SchemeDetails] = {
    val parsedSchemeDetails = splitSchemeDetails(chargeFields(fieldNoTransferSchemeDetails))

    val fields = Seq(
      Field(schemeName, parsedSchemeDetails.schemeName, schemeDetails, fieldNoTransferSchemeDetails, Some(Event1FieldNames.schemeDetails)),
      Field(schemeReference, parsedSchemeDetails.schemeReference, schemeDetails, fieldNoTransferSchemeDetails, Some(Event1FieldNames.schemeReference))
    )
    val form: Form[SchemeDetails] = schemeDetailsFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def companyDetailsValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], CompanyDetails] = {
    val fields = Seq(
      Field(companyOrOrgName, chargeFields(fieldNoCompanyOrOrgName), companyOrOrgName, fieldNoCompanyOrOrgName),
      Field(companyNo, chargeFields(fieldNoCompanyNo), companyNo, fieldNoCompanyNo)
    )
    val form: Form[CompanyDetails] = companyDetailsFormProvider()
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

  private def validatePaymentNatureMemberJourney(index: Int, columns: Seq[String], paymentNature: String) = {

    val k = resultFromFormValidationResult[MemberPaymentNature](
      genericPaymentNatureFieldValidation(index, columns, FieldInfoForValidation(fieldNoNatureOfPayment, natureOfPayment, memberPaymentNatureFormProvider()), mapPaymentNatureMember),
      createCommitItem(index, MemberPaymentNaturePage.apply)
    )

    paymentNature match {
      case "Benefit" =>
        val l = resultFromFormValidationResult[Option[String]](
          genericFieldValidation(index, columns, FieldInfoForValidation(fieldNoBenefitDescription, benefitDescription, benefitInKindBriefDescriptionFormProvider())),
          createCommitItem(index, BenefitInKindBriefDescriptionPage.apply))
        Seq(k, l).combineAll
      case "Transfer" =>
        val w = resultFromFormValidationResult[WhoWasTheTransferMade](
          whoWasTheTransferMadeValidation(index, columns), createCommitItem(index, WhoWasTheTransferMadePage.apply))
        val x = resultFromFormValidationResult[SchemeDetails](
          schemeDetailsValidation(index, columns), createCommitItem(index, SchemeDetailsPage.apply))
        Seq(k, w, x).combineAll
      case "Error" =>
        val o = resultFromFormValidationResult[Option[String]](
          genericFieldValidation(index, columns, FieldInfoForValidation(fieldNoErrorDescription, errorDescription, errorDescriptionFormProvider())),
          createCommitItem(index, ErrorDescriptionPage.apply))
        Seq(k, o).combineAll
      case "Early" =>
        val n = resultFromFormValidationResult[String](
          genericFieldValidation(index, columns, FieldInfoForValidation(fieldNoEarlyDescription, earlyDescription, benefitsPaidEarlyFormProvider())),
          createCommitItem(index, BenefitsPaidEarlyPage.apply))
        Seq(k, n).combineAll
      case "Refund" =>
        val t = resultFromFormValidationResult[RefundOfContributions](
          refundOfContributionsValidation(index, columns), createCommitItem(index, RefundOfContributionsPage.apply))
        Seq(k, t).combineAll
      case "Overpayment" =>
        val s = resultFromFormValidationResult[ReasonForTheOverpaymentOrWriteOff](
          reasonForTheOverpaymentOrWriteOffValidation(index, columns), createCommitItem(index, ReasonForTheOverpaymentOrWriteOffPage.apply))
        Seq(k, s).combineAll
      case "Residential" =>
        val u = resultFromFormValidationResult[Address](
          addressValidation(index, columns, fieldNoResidentialAddress), createCommitItem(index, ManualAddressPage(Event1MemberPropertyAddressJourney, _)))
        Seq(k, u).combineAll
      case "Tangible" =>
        val v = resultFromFormValidationResult[Option[String]](
          genericFieldValidation(index, columns, FieldInfoForValidation(fieldNoTangibleDescription, tangibleDescription, memberTangibleMoveablePropertyFormProvider())),
          createCommitItem(index, MemberTangibleMoveablePropertyPage.apply))
        Seq(k, v).combineAll
      case "Court" =>
        val m = resultFromFormValidationResult[Option[String]](
          genericFieldValidation(index, columns, FieldInfoForValidation(fieldNoCourtNameOfPersonOrOrg, courtNameOfPersonOrOrg, memberUnauthorisedPaymentRecipientNameFormProvider())),
          createCommitItem(index, UnauthorisedPaymentRecipientNamePage.apply))
        Seq(k, m).combineAll
      case "Other" =>
        val r = resultFromFormValidationResult[Option[String]](
          genericFieldValidation(index, columns, FieldInfoForValidation(fieldNoOtherDescription, otherDescription, memberPaymentNatureDescriptionFormProvider())),
          createCommitItem(index, MemberPaymentNatureDescriptionPage.apply))
        Seq(k, r).combineAll
      case _ => throw new RuntimeException("Cannot find nature of payment")

    }
  }

  private def validatePaymentNatureEmployerJourney(index: Int, columns: Seq[String], paymentNature: String) = {

    val k = resultFromFormValidationResult[EmployerPaymentNature](
      genericPaymentNatureFieldValidation(index, columns, FieldInfoForValidation(fieldNoNatureOfPayment, natureOfPayment, employerPaymentNatureFormProvider()), mapPaymentNatureEmployer),
      createCommitItem(index, EmployerPaymentNaturePage.apply)
    )

    paymentNature match {
      case "Loans" =>
        val p = resultFromFormValidationResult[LoanDetails](
          loanDetailsValidation(index, columns), createCommitItem(index, LoanDetailsPage.apply))
        Seq(k, p).combineAll
      case "Residential" =>
        val u = resultFromFormValidationResult[Address](
          addressValidation(index, columns, fieldNoResidentialAddress), createCommitItem(index, ManualAddressPage(Event1EmployerPropertyAddressJourney, _)))
        Seq(k, u).combineAll
      case "Tangible" =>
        val v = resultFromFormValidationResult[Option[String]](
          genericFieldValidation(index, columns, FieldInfoForValidation(fieldNoTangibleDescription, tangibleDescription, employerTangibleMoveablePropertyFormProvider())),
          createCommitItem(index, EmployerTangibleMoveablePropertyPage.apply))
        Seq(k, v).combineAll
      case "Court" =>
        val m = resultFromFormValidationResult[Option[String]](
          genericFieldValidation(index, columns, FieldInfoForValidation(fieldNoCourtNameOfPersonOrOrg, courtNameOfPersonOrOrg, employerUnauthorisedPaymentRecipientNameFormProvider())),
          createCommitItem(index, EmployerUnauthorisedPaymentRecipientNamePage.apply))
        Seq(k, m).combineAll
      case "Other" =>
        val r = resultFromFormValidationResult[Option[String]](
          genericFieldValidation(index, columns, FieldInfoForValidation(fieldNoOtherDescription, otherDescription, employerPaymentNatureDescriptionFormProvider())),
          createCommitItem(index, EmployerPaymentNatureDescriptionPage.apply))
        Seq(k, r).combineAll
      case _ => throw new RuntimeException("Cannot find nature of payment")

    }
  }

  override protected def validateFields(index: Int,
                                        columns: Seq[String],
                                        taxYear: Int,
                                        members: Seq[MembersDetails])
                                       (implicit messages: Messages): Result = {

    val a = resultFromFormValidationResult[WhoReceivedUnauthPayment](
      genericFieldValidation(index, columns, FieldInfoForValidation(fieldNoMemberOrEmployer, memberOrEmployer, whoReceivedUnauthPaymentFormProvider())),
      createCommitItem(index, WhoReceivedUnauthPaymentPage.apply)
    )

    a match {
      case Result(_, Valid(seqCommitItems)) =>
        seqCommitItems.headOption match {
          case Some(ci) => ci.value.as[JsString].value match {
            case "member" => memberValidation(a, index, columns, taxYear, members)
            case "employer" => employerValidation(a, index, columns, taxYear)
          }
          case _ => throw new RuntimeException("Something went wrong: member or employer not entered/found")
        }
      case invalidResult@Result(_, Invalid(_)) => invalidResult
    }
  }

  private def memberValidation(memberOrEmployerResult: Result,
                               index: Int,
                               columns: Seq[String],
                               taxYear: Int,
                               members: Seq[MembersDetails])
                              (implicit messages: Messages): Result = {
    val b = resultFromFormValidationResultForMembersDetails(
      memberDetailsValidation(index, columns, membersDetailsFormProvider(Event1, index)),
      createCommitItem(index, MembersDetailsPage.apply(Event1, _)),
      members
    )

    val c = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(fieldNoDoYouHoldSignedMandate, doYouHoldSignedMandate, doYouHoldSignedMandateFormProvider())),
      createCommitItem(index, DoYouHoldSignedMandatePage.apply)
    )

    val d = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(fieldNoValueOfUnauthorisedPayment, valueOfUnauthorisedPayment, valueOfUnauthorisedPaymentFormProvider())),
      createCommitItem(index, ValueOfUnauthorisedPaymentPage.apply)
    )

    val e = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(fieldNoSchemeUnAuthPaySurcharge, schemeUnAuthPaySurcharge, schemeUnAuthPaySurchargeMemberFormProvider())),
      createCommitItem(index, SchemeUnAuthPaySurchargeMemberPage.apply)
    )

    val paymentNature = validatePaymentNatureMemberJourney(index, columns, columns(10))

    val y = resultFromFormValidationResult[PaymentDetails](
      paymentValueAndDateValidation(index, columns, taxYear), createCommitItem(index, PaymentValueAndDatePage.apply)
    )

    Seq(memberOrEmployerResult, b, c, d, e, paymentNature, y).combineAll
  }

  private def employerValidation(memberOrEmployerResult: Result,
                                 index: Int,
                                 columns: Seq[String],
                                 taxYear: Int)
                                (implicit messages: Messages): Result = {
    val h = resultFromFormValidationResult[CompanyDetails](
      companyDetailsValidation(index, columns), createCommitItem(index, CompanyDetailsPage.apply)
    )

    val j = resultFromFormValidationResult[Address](
      addressValidation(index, columns, fieldNoCompanyAddress), createCommitItem(index, ManualAddressPage(Event1EmployerAddressJourney, _))
    )

    val paymentNature = validatePaymentNatureEmployerJourney(index, columns, columns(10))

    val y = resultFromFormValidationResult[PaymentDetails](
      paymentValueAndDateValidation(index, columns, taxYear), createCommitItem(index, PaymentValueAndDatePage.apply)
    )

    Seq(memberOrEmployerResult, h, j, paymentNature, y).combineAll
  }
}
