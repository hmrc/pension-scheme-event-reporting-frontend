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

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.toFoldableOps
import config.FrontendAppConfig
import forms.common.MembersDetailsFormProvider
import forms.event24._
import models.Index
import models.common.MembersDetails
import models.enumeration.EventType
import models.enumeration.EventType.Event24
import models.event24.{BCETypeSelection, CrystallisedDate, ProtectionReferenceData, TypeOfProtectionGroup1, TypeOfProtectionGroup2}
import models.fileUpload.FileUploadHeaders.Event24FieldNames._
import models.fileUpload.FileUploadHeaders.{Event24FieldNames, MemberDetailsFieldNames, valueFormField}
import pages.common.MembersDetailsPage
import pages.event24._
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import services.fileUpload.Validator.Result

import java.time.LocalDate
import javax.inject.Inject
import scala.annotation.tailrec

class Event24Validator @Inject()(
                                  membersDetailsFormProvider: MembersDetailsFormProvider,
                                  bceTypeSelectionFormProvider: BCETypeSelectionFormProvider,
                                  crystallisedDateFormProvider: CrystallisedDateFormProvider,
                                  employerPayeReferenceFormProvider: EmployerPayeReferenceFormProvider,
                                  marginalRateFormProvider: MarginalRateFormProvider,
                                  overAllowanceFormProvider: OverAllowanceFormProvider,
                                  overAllowanceAndDeathBenefitFormProvider: OverAllowanceAndDeathBenefitFormProvider,
                                  totalAmountBenefitCrystallisationFormProvider: TotalAmountBenefitCrystallisationFormProvider,
                                  typeOfProtectionGroup1FormProvider: TypeOfProtectionGroup1FormProvider,
                                  typeOfProtectionGroup1ReferenceFormProvider: TypeOfProtectionGroup1ReferenceFormProvider,
                                  typeOfProtectionGroup2FormProvider: TypeOfProtectionGroup2FormProvider,
                                  typeOfProtectionGroup2ReferenceFormProvider: TypeOfProtectionGroup2ReferenceFormProvider,
                                  validProtectionFormProvider: ValidProtectionFormProvider,
                                  config: FrontendAppConfig
                                ) extends Validator {

  override val eventType: EventType = EventType.Event24

  override def validHeader: String = config.validEvent24Header

  // TODO - check column numbers
  override protected val fieldNoFirstName = 1
  override protected val fieldNoLastName = 2
  override protected val fieldNoNino = 3
  private val fieldNoBCEDate = 4
  private val fieldNoBCEType = 5
  private val fieldNoAmount = 6
  private val fieldNoValidProtection = 7

  private val fieldNoProtectionTypeGroup2 = 9
  private val fieldNoProtectionReferenceGroup2 = 10

  private val fieldNoProtectionTypeGroup1 = 11
  private val fieldNoNonResidenceRef = 12
  private val fieldNoPensionCreditsRef = 13
  private val fieldNoPreCommencementRef = 14
  private val fieldNoOverseasRef = 15
  private val fieldNoSchemeSpecific = 16

  private val fieldNoOverAllowanceAndDeathBenefit = 17
  private val fieldNoOverAllowance = 18
  private val fieldNoMarginalRate = 20
  private val fieldNoPAYERef = 22

  private val mapBCEType: Map[String, String] = {
    Map(
      "ANN" -> "annuityProtection",
      "DEF" -> "definedBenefit",
      "DRAW" -> "drawdown",
      "FLEXI" -> "flexiAccess",
      "PROTECTION" -> "pensionProtection",
      "SERIOUS" -> "seriousHealthLumpSum",
      "STAND" -> "standAlone",
      "UN LS" -> "uncrystallisedFunds",
      "UN DB" -> "uncrystallisedFundsDeathBenefit"
    )
  }
  private val hideMarginalRateValues : Seq[String] = Seq("ANN","DEF","DRAW","FLEXI","PROTECTION","UN DB")

  private val mapProtectionTypeGroup2: Map[String, String] = {
    Map(
      "ENHANCED" -> "enhancedProtection",
      "ENHANCED 375" -> "enhancedProtectionWithProtectedSum",
      "FIXED" -> "fixedProtection",
      "FP 2014" -> "fixedProtection2014",
      "FP 2016" -> "fixedProtection2016",
      "IP 2014" -> "individualProtection2014",
      "IP 2016" -> "individualProtection2016",
      "PRIMARY" -> "primary",
      "PRIMARY 375" -> "primaryWithProtectedSum",
      "" -> "noOtherProtections"
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

  private def protectionGroup1Validation(index: Index, chargeFields: Seq[String]): Validated[Seq[ValidationError], Set[TypeOfProtectionGroup1]] = {
    val spreadsheetValues: Array[String] = chargeFields(fieldNoProtectionTypeGroup1).filterNot(_.isWhitespace).split(",")

    def getProtectionTypeString(typeCode: String) = typeCode match {
      case "NON-RESIDENCE" => Map("value[0]" -> "nonResidenceEnhancement")
      case "CREDITS" => Map("value[1]" -> "pensionCreditsPreCRE")
      case "PRE-COMM" => Map("value[2]" -> "preCommencement")
      case "OVERSEAS" => Map("value[3]" -> "recognisedOverseasPSTE")
      case "SS" => Map("value[4]" -> "schemeSpecific")
      case _ => Map("value[0]" -> "noneOfTheAbove")
    }

    @tailrec
    def generateFields(spreadsheetValues: Array[String], fields: Seq[Field]): Seq[Field] = {
      if (spreadsheetValues.isEmpty) {
        fields
      } else {
        val fieldMap = getProtectionTypeString(spreadsheetValues.head)
        val field = Field(fieldMap.head._1, fieldMap.head._2, protectionTypeGroup1, fieldNoProtectionTypeGroup1)
        generateFields(spreadsheetValues.tail, fields ++ Seq(field))
      }
    }

    val protectionTypeFields = generateFields(spreadsheetValues, Seq.empty)

    val form: Form[Set[TypeOfProtectionGroup1]] = typeOfProtectionGroup1FormProvider()

    form.bind(Field.seqToMap(protectionTypeFields)).fold(
        formWithErrors => Invalid(errorsFromForm(formWithErrors, protectionTypeFields, index)),
        value => Valid(value)
    )
  }

  private def protectionReferenceGroup1Validation(index: Index, chargeFields: Seq[String]): Validated[Seq[ValidationError], ProtectionReferenceData] = {
    val fields = Seq(
      Field(Event24FieldNames.nonResidenceReference,
        chargeFields(fieldNoNonResidenceRef), Event24FieldNames.nonResidenceReference, fieldNoNonResidenceRef),
      Field(Event24FieldNames.pensionCreditsReference,
        chargeFields(fieldNoPensionCreditsRef), Event24FieldNames.pensionCreditsReference, fieldNoPensionCreditsRef),
      Field(Event24FieldNames.preCommencementReference,
        chargeFields(fieldNoPreCommencementRef), Event24FieldNames.preCommencementReference, fieldNoPreCommencementRef),
      Field(Event24FieldNames.overseasReference,
        chargeFields(fieldNoOverseasRef), Event24FieldNames.overseasReference, fieldNoOverseasRef),
      Field(Event24FieldNames.schemeSpecific,
        chargeFields(fieldNoSchemeSpecific), Event24FieldNames.schemeSpecific, fieldNoSchemeSpecific)
    )

    val form: Form[ProtectionReferenceData] = typeOfProtectionGroup1ReferenceFormProvider()

    form.bind(Field.seqToMap(fields)).fold(
      _ => {
        val refTypesForm = form.bind(Field.seqToMap(fields))
        val selectedProtectionTypes: Array[String] = chargeFields(fieldNoProtectionTypeGroup1).filterNot(_.isWhitespace).split(",")

        val requiredReferenceTypes: Seq[TypeOfProtectionGroup1] = selectedProtectionTypes.map { typeCode => {
          typeCode match {
            case "NON-RESIDENCE" => TypeOfProtectionGroup1.NonResidenceEnhancement
            case "CREDITS" => TypeOfProtectionGroup1.PensionCreditsPreCRE
            case "PRE-COMM" => TypeOfProtectionGroup1.PreCommencement
            case "OVERSEAS" => TypeOfProtectionGroup1.RecognisedOverseasPSTE
            case "SS" => TypeOfProtectionGroup1.SchemeSpecific
            case _ => TypeOfProtectionGroup1.NoneOfTheAbove
          }
        }}

        val validErrors = getValidErrors(refTypesForm.errors, Seq.empty, requiredReferenceTypes)

        if (validErrors.nonEmpty) {
          val formWithValidErrors = refTypesForm.copy(errors = validErrors)
          Invalid(errorsFromForm(formWithValidErrors, fields, index))
        } else {
          val formData = refTypesForm.data
          val value = getUserAnswers(formData)
          Valid(value)
        }
      },
      value => Valid(value)
    )
  }

  @tailrec
  private def getValidErrors(allErrors: Seq[FormError], validErrors: Seq[FormError], requiredReferenceTypes: Seq[TypeOfProtectionGroup1]): Seq[FormError] = {
    if (allErrors.isEmpty) {
      validErrors
    } else if (requiredReferenceTypes.map(_.toString).contains(allErrors.head.key)) {
      val errors = validErrors ++ Seq(allErrors.head)
      getValidErrors(allErrors.tail, errors, requiredReferenceTypes)
    } else {
      getValidErrors(allErrors.tail, validErrors, requiredReferenceTypes)
    }
  }

  private def getUserAnswers(formData: Map[String, String]): ProtectionReferenceData = {
    val nonResidenceEnhancement = formData.getOrElse("nonResidenceEnhancement", "")
    val pensionCreditsPreCRE = formData.getOrElse("pensionCreditsPreCRE", "")
    val preCommencement = formData.getOrElse("preCommencement", "")
    val recognisedOverseasPSTE = formData.getOrElse("recognisedOverseasPSTE", "")
    ProtectionReferenceData(nonResidenceEnhancement, pensionCreditsPreCRE, preCommencement, recognisedOverseasPSTE)
  }

  private def protectionGroup2Validation(index: Index, chargeFields: Seq[String]): Validated[Seq[ValidationError], TypeOfProtectionGroup2] = {
    val mappedProtectionType = mapProtectionTypeGroup2.applyOrElse[String, String](
      chargeFields(fieldNoProtectionTypeGroup2),
      (_: String) => "Type of protection is not found or doesn't exist")

    val fields = Seq(
      Field(valueFormField, mappedProtectionType, protectionTypeGroup2, fieldNoProtectionTypeGroup2)
    )

    val form: Form[TypeOfProtectionGroup2] = typeOfProtectionGroup2FormProvider()

    form.bind(Field.seqToMap(fields)).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => {
        Valid(value)
      }
    )
  }

  private def protectionGroup2ReferenceValidation(index: Index, chargeFields: Seq[String]): Validated[Seq[ValidationError], String] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoProtectionReferenceGroup2), protectionTypeGroup2Reference, fieldNoProtectionReferenceGroup2)
    )

    val form: Form[String] = typeOfProtectionGroup2ReferenceFormProvider()

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

  override protected def memberDetailsValidation(index: Int, columns: Seq[String],
                                        memberDetailsForm: Form[MembersDetails]): Validated[Seq[ValidationError], MembersDetails] = {
    val fields = Seq(
      Field(MemberDetailsFieldNames.firstName, fieldValue(columns, fieldNoFirstName), MemberDetailsFieldNames.firstName, fieldNoFirstName),
      Field(MemberDetailsFieldNames.lastName, fieldValue(columns, fieldNoLastName), MemberDetailsFieldNames.lastName, fieldNoLastName),
      Field(MemberDetailsFieldNames.nino, fieldValue(columns, fieldNoNino), MemberDetailsFieldNames.nino, fieldNoNino)
    )

    val toMap = Field.seqToMap(fields)

    val bind = memberDetailsForm.bind(toMap)
    bind.fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  override def validateFields(index: Int,
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
    val marginalRateValue = columns(fieldNoMarginalRate)

    val l = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(fieldNoMarginalRate, marginalRate, marginalRateFormProvider())),
      createCommitItem(index, MarginalRatePage.apply(_))
    )

    marginalRateValue match {
      case "YES" =>
        val m = resultFromFormValidationResult[String](
          payeReferenceValidation(index, columns), createCommitItem(index, EmployerPayeReferencePage.apply(_))
        )
        Seq(l, m).combineAll
      case _ => l
    }
  }

  private def validateOverAllowanceDBA(index: Index, columns: Seq[String]): Result = {
    val overAllowanceAndDeathBenefitValue = columns(fieldNoOverAllowanceAndDeathBenefit)
    val overAllowanceValue = columns(fieldNoOverAllowance)

    val j = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(
        fieldNoOverAllowanceAndDeathBenefit, overAllowanceAndDeathBenefit, overAllowanceAndDeathBenefitFormProvider())
      ),
      createCommitItem(index, OverAllowanceAndDeathBenefitPage.apply(_))
    )

    val k = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(fieldNoOverAllowance, overAllowance, overAllowanceFormProvider())),
      createCommitItem(index, OverAllowancePage.apply(_))
    )

    val defaultMarginalRateNo = hideMarginalRateValues.contains(columns(fieldNoBCEType))
    val marginalRate = validateMarginalRate(index, columns)

    (defaultMarginalRateNo, overAllowanceAndDeathBenefitValue, overAllowanceValue) match {
      case (true, "YES", _) =>
        Seq(j).combineAll
      case (true, "NO", "YES") =>
        Seq(j, k).combineAll
      case (false, "YES", _) =>
        Seq(j, marginalRate).combineAll
      case (false, "NO", "YES") =>
        Seq(j, k, marginalRate).combineAll
      case _ => Seq(j, k).combineAll
    }
  }

  private def validateTypeOfProtectionGroup1(index: Index, columns: Seq[String]): Result = {
    val protectionTypeValue = columns(fieldNoProtectionTypeGroup1)

    val h = resultFromFormValidationResult[Set[TypeOfProtectionGroup1]](
      protectionGroup1Validation(index, columns),
      createCommitItem(index, TypeOfProtectionGroup1Page.apply(_))
    )

    val i = resultFromFormValidationResult[ProtectionReferenceData](
      protectionReferenceGroup1Validation(index, columns),
      createCommitItem(index, TypeOfProtectionGroup1ReferencePage.apply)
    )

    val overAllowanceDBA = validateOverAllowanceDBA(index, columns)

    protectionTypeValue match {
      case "SS" => Seq(h, overAllowanceDBA).combineAll
      case _ => Seq(h, i, overAllowanceDBA).combineAll
    }
  }

  private def validateTypeOfProtectionGroup2(index: Index, columns: Seq[String]): Result = {
    val protectionTypeValue = columns(fieldNoProtectionTypeGroup2)

    val f = resultFromFormValidationResult[TypeOfProtectionGroup2](
      protectionGroup2Validation(index, columns),
      createCommitItem(index, TypeOfProtectionGroup2Page.apply(_))
    )

    val g = resultFromFormValidationResult[String](
      protectionGroup2ReferenceValidation(index, columns),
      createCommitItem(index, TypeOfProtectionGroup2ReferencePage.apply(_))
    )

    val typeOfProtectionGroup1 = validateTypeOfProtectionGroup1(index, columns)

    protectionTypeValue match {
      case "" => Seq(f, typeOfProtectionGroup1).combineAll
      case _ => Seq(f, g, typeOfProtectionGroup1).combineAll
    }
  }


  private def validateValidProtection(index: Index, columns: Seq[String]): Result = {
    val validProtectionValue = columns(fieldNoValidProtection)

    val e = resultFromFormValidationResult[Boolean](
      genericBooleanFieldValidation(index, columns, FieldInfoForValidation(fieldNoValidProtection, validProtection, validProtectionFormProvider())),
      createCommitItem(index, ValidProtectionPage.apply(_))
    )

    validProtectionValue match {
      case "YES" => Seq(e, validateTypeOfProtectionGroup2(index, columns)).combineAll
      case _ => Seq(e, validateOverAllowanceDBA(index, columns)).combineAll
    }
  }
}
