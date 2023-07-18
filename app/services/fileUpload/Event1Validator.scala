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
import forms.common.MembersDetailsFormProvider
import forms.event1._
import forms.event1.member.{SchemeDetailsFormProvider, WhoWasTheTransferMadeFormProvider}
import models.common.MembersDetails
import models.enumeration.EventType
import models.enumeration.EventType.Event6
import models.event1.member.{SchemeDetails, WhoWasTheTransferMade}
import models.event1.{PaymentDetails, PaymentNature}
import models.fileUpload.FileUploadHeaders.Event6FieldNames._
import models.fileUpload.FileUploadHeaders.{Event1FieldNames, valueFormField}
import pages.common.MembersDetailsPage
import pages.event1.PaymentValueAndDatePage
import play.api.data.Form
import play.api.i18n.Messages
import services.fileUpload.Validator.Result

class Event1Validator @Inject()(
                                 membersDetailsFormProvider: MembersDetailsFormProvider,
                                 doYouHoldSignedMandateFormProvider: DoYouHoldSignedMandateFormProvider,
                                 paymentValueAndDateFormProvider: PaymentValueAndDateFormProvider,
                                 valueOfUnauthorisedPaymentFormProvider: ValueOfUnauthorisedPaymentFormProvider,
                                 schemeUnAuthPaySurchargeMemberFormProvider: SchemeUnAuthPaySurchargeMemberFormProvider,
                                 paymentNatureFormProvider: PaymentNatureFormProvider,
                                 benefitInKindBriefDescriptionFormProvider: BenefitInKindBriefDescriptionFormProvider,
                                 whoWasTheTransferMadeFormProvider: WhoWasTheTransferMadeFormProvider,
                                 schemeDetailsFormProvider: SchemeDetailsFormProvider,
                                 config: FrontendAppConfig
                               ) extends Validator {

  override val eventType: EventType = EventType.Event1

  override protected def validHeader: String = config.validEvent1Header //TODO: This needs to be updated

  private val fieldNoTypeOfProtection = 3
  private val fieldNoTypeOfProtectionReference = 4
  private val fieldNoPaymentAmount = 5
  private val fieldNoPaymentDate = 6

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

  private val mapPaymentNatureEmployer: Map[String, String] = {
    Map(
      "Loans" -> "loansExceeding50PercentOfFundValue",
      "Residential" -> "residentialProperty",
      "Tangible" -> "tangibleMoveableProperty",
      "Court" -> "courtOrder",
      "Other" -> "employerOther"
    )
  }

  private def doYouHoldSignedMandateValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Boolean] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTypeOfProtectionReference), typeOfProtectionReference, fieldNoTypeOfProtectionReference)
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
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTypeOfProtectionReference), typeOfProtectionReference, fieldNoTypeOfProtectionReference)
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
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTypeOfProtectionReference), typeOfProtectionReference, fieldNoTypeOfProtectionReference)
    )
    val form: Form[Boolean] = schemeUnAuthPaySurchargeMemberFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def paymentNatureValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], PaymentNature] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTypeOfProtectionReference), typeOfProtectionReference, fieldNoTypeOfProtectionReference)
    )
    val form: Form[PaymentNature] = paymentNatureFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def benefitInKindBriefDescriptionValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], Option[String]] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTypeOfProtectionReference), typeOfProtectionReference, fieldNoTypeOfProtectionReference)
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
      Field(valueFormField, chargeFields(fieldNoTypeOfProtectionReference), typeOfProtectionReference, fieldNoTypeOfProtectionReference)
    )
    val form: Form[WhoWasTheTransferMade] = whoWasTheTransferMadeFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def schemeDetailsFormValidation(index: Int, chargeFields: Seq[String]): Validated[Seq[ValidationError], SchemeDetails] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTypeOfProtectionReference), typeOfProtectionReference, fieldNoTypeOfProtectionReference)
    )
    val form: Form[SchemeDetails] = schemeDetailsFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  //noinspection ScalaStyle
  private def paymentValueAndDateValidation(index: Int,
                                            chargeFields: Seq[String],
                                            taxYear: Int)
                                           (implicit messages: Messages): Validated[Seq[ValidationError], PaymentDetails] = {

    val parsedDate = splitDayMonthYear(chargeFields(fieldNoPaymentDate))

    val fields = Seq(
      Field(lumpSumAmount, chargeFields(fieldNoPaymentAmount), lumpSumAmount, fieldNoPaymentAmount),
      Field(dateOfEventDay, parsedDate.day, lumpSumDate, fieldNoPaymentDate, Some(Event1FieldNames.paymentDate)),
      Field(dateOfEventMonth, parsedDate.month, lumpSumDate, fieldNoPaymentDate, Some(Event1FieldNames.paymentDate)),
      Field(dateOfEventYear, parsedDate.year, lumpSumDate, fieldNoPaymentDate, Some(Event1FieldNames.paymentDate))
    )

    val form: Form[PaymentDetails] = paymentValueAndDateFormProvider(taxYear)

    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  override protected def validateFields(index: Int,
                                        columns: Seq[String],
                                        taxYear: Int)
                                       (implicit messages: Messages): Result = {
    val a = resultFromFormValidationResult[MembersDetails](
      memberDetailsValidation(index, columns, membersDetailsFormProvider(Event6, index)),
      createCommitItem(index, MembersDetailsPage.apply(Event6, _))
    )

    val b = resultFromFormValidationResult[PaymentDetails](
      paymentValueAndDateValidation(index, columns, taxYear), createCommitItem(index, PaymentValueAndDatePage.apply(_))
    )

    Seq(a, b).combineAll
  }
}
