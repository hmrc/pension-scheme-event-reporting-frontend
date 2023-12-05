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
import config.FrontendAppConfig
import forms.common.MembersDetailsFormProvider
import forms.event24._
import models.Index
import models.enumeration.EventType
import models.enumeration.EventType.Event24
import models.event24.{BCETypeSelection, CrystallisedDate, TypeOfProtectionSelection}
import models.fileUpload.FileUploadHeaders.Event24FieldNames._
import models.fileUpload.FileUploadHeaders.valueFormField
import pages.common.MembersDetailsPage
import pages.event24._
import play.api.data.Form
import play.api.i18n.Messages
import services.fileUpload.Validator.Result

import java.time.LocalDate
import javax.inject.Inject

class Event24Validator @Inject()(
                                  membersDetailsFormProvider: MembersDetailsFormProvider,
                                  bceTypeSelectionFormProvider: BCETypeSelectionFormProvider,
                                  crystallisedDateFormProvider: CrystallisedDateFormProvider,
                                  employerPayeReferenceFormProvider: EmployerPayeReferenceFormProvider,
                                  marginalRateFormProvider: MarginalRateFormProvider,
                                  overAllowanceFormProvider: OverAllowanceFormProvider,
                                  overAllowanceAndDeathBenefitFormProvider: OverAllowanceAndDeathBenefitFormProvider,
                                  totalAmountBenefitCrystallisationFormProvider: TotalAmountBenefitCrystallisationFormProvider,
                                  typeOfProtectionFormProvider: TypeOfProtectionFormProvider,
                                  typeOfProtectionReferenceFormProvider: TypeOfProtectionReferenceFormProvider,
                                  validProtectionFormProvider: ValidProtectionFormProvider,
                                  config: FrontendAppConfig
                                ) extends Validator {

  override val eventType: EventType = EventType.Event24

  override protected def validHeader: String = config.validEvent24Header

  private val fieldNoBCEDate = 3
  private val fieldNoBCEType = 4
  private val fieldNoAmount = 5
  private val fieldNoValidProtection = 6
  private val fieldNoProtectionType = 7
  private val fieldNoProtectionReference = 8
  private val fieldNoOverAllowance = 9
  private val fieldNoOverAllowanceAndDeathBenefit = 10
  private val fieldNoMarginalRate = 11
  private val fieldNoPAYERef = 12

  private val mapBCEType: Map[String, String] = {
    Map(
      "ANN" -> "annuityProtection",
      "DEF" -> "definedBenefit",
      "DRAW" -> "drawdown",
      "FLEXI" -> "flexiAccess",
      "PCLS" -> "commencement",
      "PROTECTION" -> "pensionProtection",
      "SMALL" -> "small",
      "S-A" -> "standAlone",
      "T LS" -> "trivialCommutation",
      "T DB" -> "trivialCommutationDeathBenefit",
      "SERIOUS" -> "seriousHealthLumpSum",
      "UN LS" -> "uncrystallisedFunds",
      "UN DB" -> "uncrystallisedFundsDeathBenefit",
      "WIND-UP" -> "windingUp"
    )
  }

  private val mapProtectionType: Map[String, String] = {
    Map(
      "EN" -> "enhancedProtection",
      "EN 375" -> "enhancedProtectionWithProtectedSum",
      "FIXED" -> "fixedProtection",
      "FP 2014" -> "fixedProtection2014",
      "FP 2016" -> "fixedProtection2016",
      "IP 2014" -> "individualProtection2014",
      "IP 2016" -> "individualProtection2016",
      "NON" -> "nonResidenceEnhancement",
      "CREDITS" -> "pensionCreditsPreCRE",
      "PRE-COMM" -> "preCommencement",
      "PRIMARY" -> "primary",
      "LS 375" -> "primaryWithProtectedSum",
      "OVERSEAS" -> "recognisedOverseasPSTE",
      "SPEC" -> "schemeSpecific"
    )
  }

  private def bceDateValidation(index: Index,
                                chargeFields: Seq[String],
                                taxYear: Int)(implicit messages: Messages): Validated[Seq[ValidationError], CrystallisedDate] = {
    val minDate = LocalDate.of(taxYear, 4, 6)
    val maxDate = LocalDate.of(taxYear + 1, 4, 5)
    val parsedDate = splitDayMonthYear(chargeFields(fieldNoBCEDate))

    val fields = Seq(
      Field(dateOfEventDay, parsedDate.day, crystallisedDate, fieldNoBCEDate, Some(crystallisedDate)),
      Field(dateOfEventMonth, parsedDate.month, crystallisedDate, fieldNoBCEDate, Some(crystallisedDate)),
      Field(dateOfEventYear, parsedDate.year, crystallisedDate, fieldNoBCEDate, Some(crystallisedDate))
    )

    val form: Form[CrystallisedDate] = crystallisedDateFormProvider(minDate, maxDate)

    form.bind(Field.seqToMap(fields)).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def bceTypeValidation(index: Index, chargeFields: Seq[String]): Validated[Seq[ValidationError], BCETypeSelection] = {
    val mappedBCEType = mapBCEType.applyOrElse[String, String](
      chargeFields(fieldNoBCEType),
      (_: String) => "Type of protection is not found or doesn't exist")

    val fields = Seq(
      Field(valueFormField, mappedBCEType, bceType, fieldNoBCEType)
    )

    val form: Form[BCETypeSelection] = bceTypeSelectionFormProvider()

    form.bind(Field.seqToMap(fields)).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def amountValidation(index: Index, chargeFields: Seq[String]): Validated[Seq[ValidationError], BigDecimal] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoAmount), totalAmount, fieldNoAmount)
    )

    val form: Form[BigDecimal] = totalAmountBenefitCrystallisationFormProvider()

    form.bind(Field.seqToMap(fields)).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def toBoolean(s: String): String = s match {
    case "YES" => "true"
    case "NO" => "false"
    case _ => s
  }

  private case class FieldInfoForValidation[A](fieldNum: Int, description: String, form: Form[A])


  private def genericBooleanFieldValidation(
                                             index: Index,
                                             chargeFields: Seq[String],
                                             fieldInfoForValidation: FieldInfoForValidation[Boolean]
                                           ): Validated[Seq[ValidationError], Boolean] = {
    val mappedBoolean = toBoolean(chargeFields(fieldInfoForValidation.fieldNum))
    val fields = Seq(Field(valueFormField, mappedBoolean, fieldInfoForValidation.description, fieldInfoForValidation.fieldNum))
    fieldInfoForValidation.form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def protectionTypeValidation(index: Index, chargeFields: Seq[String]): Validated[Seq[ValidationError], TypeOfProtectionSelection] = {
    val mappedProtectionType = mapProtectionType.applyOrElse[String, String](
      chargeFields(fieldNoProtectionType),
      (_: String) => "Type of protection is not found or doesn't exist")

    val fields = Seq(
      Field(valueFormField, mappedProtectionType, protectionType, fieldNoProtectionType)
    )

    val form: Form[TypeOfProtectionSelection] = typeOfProtectionFormProvider()

    form.bind(Field.seqToMap(fields)).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def protectionReferenceValidation(index: Index, chargeFields: Seq[String]): Validated[Seq[ValidationError], String] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoProtectionReference), protectionReference, fieldNoProtectionReference)
    )

    val form: Form[String] = typeOfProtectionReferenceFormProvider()

    form.bind(Field.seqToMap(fields)).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def payeReferenceValidation(index: Index, chargeFields: Seq[String]): Validated[Seq[ValidationError], String] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoPAYERef), employerPayeRef, fieldNoPAYERef)
    )

    val form: Form[String] = employerPayeReferenceFormProvider()

    form.bind(Field.seqToMap(fields)).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  override protected def validateFields(index: Int,
                                        columns: Seq[String],
                                        taxYear: Int)
                                       (implicit messages: Messages): Result = {
    val a = resultFromFormValidationResultForMembersDetails(
      memberDetailsValidation(index, columns, membersDetailsFormProvider(Event24, index)),
      createCommitItem(index, MembersDetailsPage.apply(Event24, _))
    )

    val b = resultFromFormValidationResult[CrystallisedDate](
      bceDateValidation(index, columns, taxYear), createCommitItem(index, CrystallisedDatePage.apply)
    )

    val c = resultFromFormValidationResult[BCETypeSelection](bceTypeValidation(index, columns), createCommitItem(index, BCETypeSelectionPage.apply(_)))

    val d = resultFromFormValidationResult[BigDecimal](
      amountValidation(index, columns), createCommitItem(index, TotalAmountBenefitCrystallisationPage.apply(_))
    )

    val validProtection = validateValidProtection(index, columns)

    Seq(a, b, c, d, validProtection).combineAll
  }

  private def validateMarginalRate(index: Index, columns: Seq[String]): Result = {
    val marginalRateValue = columns(11)

    val j = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(fieldNoMarginalRate, marginalRate, marginalRateFormProvider())),
      createCommitItem(index, MarginalRatePage.apply(_))
    )

    marginalRateValue match {
      case "YES" =>
        val k = resultFromFormValidationResult[String](
          payeReferenceValidation(index, columns), createCommitItem(index, EmployerPayeReferencePage.apply(_))
        )

        Seq(j, k).combineAll
      case _ => j
    }
  }

  private def validateOverAllowance(index: Index, columns: Seq[String]): Result = {
    val overAllowanceValue = columns(9)
    val overAllowanceAndDeathBenefitValue = columns(10)

    val h = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(fieldNoOverAllowance, overAllowance, overAllowanceFormProvider())),
      createCommitItem(index, OverAllowancePage.apply(_))
    )

    val i = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(
        fieldNoOverAllowanceAndDeathBenefit, overAllowanceAndDeathBenefit, overAllowanceAndDeathBenefitFormProvider())
      ),
      createCommitItem(index, OverAllowanceAndDeathBenefitPage.apply(_))
    )

    val marginalRate = validateMarginalRate(index, columns)

    (overAllowanceValue, overAllowanceAndDeathBenefitValue) match {
      case ("YES", _) =>
        Seq(h, marginalRate).combineAll
      case ("NO", "YES") =>
        Seq(h, i, marginalRate).combineAll
      case _ => Seq(h, i).combineAll
    }
  }

  private def validateProtectionType(index: Index, columns: Seq[String]): Result = {
    val protectionTypeValue = columns(7)

    val f = resultFromFormValidationResult[TypeOfProtectionSelection](
      protectionTypeValidation(index, columns), createCommitItem(index, TypeOfProtectionPage.apply(_))
    )

    val g = resultFromFormValidationResult[String](
      protectionReferenceValidation(index, columns), createCommitItem(index, TypeOfProtectionReferencePage.apply)
    )

    val overAllowance = validateOverAllowance(index, columns)

    protectionTypeValue match {
      case "SPEC" => Seq(f, overAllowance).combineAll
      case _ => Seq(f, g, overAllowance).combineAll
    }
  }

  private def validateValidProtection(index: Index, columns: Seq[String]): Result = {
    val validProtectionValue = columns(6)

    val e = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(fieldNoValidProtection, validProtection, validProtectionFormProvider())),
      createCommitItem(index, ValidProtectionPage.apply(_))
    )

    validProtectionValue match {
      case "YES" => Seq(e, validateProtectionType(index, columns)).combineAll
      case _ => e
    }
  }
}
