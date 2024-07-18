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
import com.google.inject.Inject
import config.FrontendAppConfig
import forms.common.MembersDetailsFormProvider
import forms.event6.{AmountCrystallisedAndDateFormProvider, InputProtectionTypeFormProvider, TypeOfProtectionFormProvider}
import models.enumeration.EventType
import models.enumeration.EventType.Event6
import models.event6.{CrystallisedDetails, TypeOfProtection}
import models.fileUpload.FileUploadHeaders.Event6FieldNames._
import models.fileUpload.FileUploadHeaders.{Event6FieldNames, valueFormField}
import pages.common.MembersDetailsPage
import pages.event6.{AmountCrystallisedAndDatePage, InputProtectionTypePage, TypeOfProtectionPage}
import play.api.data.Form
import play.api.i18n.Messages
import services.fileUpload.Validator.Result

import java.time.LocalDate

class Event6Validator @Inject()(
                                 membersDetailsFormProvider: MembersDetailsFormProvider,
                                 typeOfProtectionFormProvider: TypeOfProtectionFormProvider,
                                 inputProtectionTypeFormProvider: InputProtectionTypeFormProvider,
                                 amountCrystallisedAndDateFormProvider: AmountCrystallisedAndDateFormProvider,
                                 config: FrontendAppConfig
                               ) extends Validator {
  override val eventType: EventType = EventType.Event6

  override def validHeader: String = config.validEvent6Header

  private val fieldNoTypeOfProtection = 3
  private val fieldNoTypeOfProtectionReference = 4
  private val fieldNoLumpSumAmount = 5
  private val fieldNoLumpSumDate = 6

  private val mapTypeOfProtection: Map[String, String] = {
    Map(
      "enhanced lifetime allowance" -> "enhancedLifetimeAllowance",
      "enhanced protection" -> "enhancedProtection",
      "fixed protection" -> "fixedProtection",
      "fixed protection 2014" -> "fixedProtection2014",
      "fixed protection 2016" -> "fixedProtection2016",
      "individual protection 2014" -> "individualProtection2014",
      "individual protection 2016" -> "individualProtection2016"
    )
  }

  private def typeOfProtectionValidation(index: Int,
                                         chargeFields: Seq[String]): Validated[Seq[ValidationError], TypeOfProtection] = {

    val mappedTypeOfProtection = mapTypeOfProtection.applyOrElse[String, String](chargeFields(fieldNoTypeOfProtection),
      (_: String) => "Type of protection is not found or doesn't exist")

    val fields = Seq(
      Field(valueFormField, mappedTypeOfProtection, typeOfProtection, fieldNoTypeOfProtection)
    )
    val form: Form[TypeOfProtection] = typeOfProtectionFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def typeOfProtectionReferenceValidation(index: Int,
                                                  chargeFields: Seq[String]): Validated[Seq[ValidationError], String] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTypeOfProtectionReference), typeOfProtectionReference, fieldNoTypeOfProtectionReference)
    )
    val form: Form[String] = inputProtectionTypeFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  //noinspection ScalaStyle
  private def amountCrystallisedAndDateValidation(index: Int,
                                                  chargeFields: Seq[String],
                                                  taxYear: Int)
                                                 (implicit messages: Messages): Validated[Seq[ValidationError], CrystallisedDetails] = {

    val maxDate = LocalDate.of(taxYear + 1, 4, 5)
    val parsedDate = splitDayMonthYear(chargeFields(fieldNoLumpSumDate))

    val fields = Seq(
      Field(lumpSumAmount, chargeFields(fieldNoLumpSumAmount), lumpSumAmount, fieldNoLumpSumAmount),
      Field(dateOfEventDay, parsedDate.day, lumpSumDate, fieldNoLumpSumDate, Some(Event6FieldNames.lumpSumDate)),
      Field(dateOfEventMonth, parsedDate.month, lumpSumDate, fieldNoLumpSumDate, Some(Event6FieldNames.lumpSumDate)),
      Field(dateOfEventYear, parsedDate.year, lumpSumDate, fieldNoLumpSumDate, Some(Event6FieldNames.lumpSumDate))
    )

    val form: Form[CrystallisedDetails] = amountCrystallisedAndDateFormProvider(maxDate)

    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  override def validateFields(index: Int,
                                        columns: Seq[String],
                                        taxYear: Int)
                                       (implicit messages: Messages): Result = {

    val a = resultFromFormValidationResultForMembersDetails(
      memberDetailsValidation(index, columns, membersDetailsFormProvider(Event6, index)),
      createCommitItem(index, MembersDetailsPage.apply(Event6, _))
    )

    val b = resultFromFormValidationResult[TypeOfProtection](
      typeOfProtectionValidation(index, columns), createCommitItem(index, TypeOfProtectionPage.apply(Event6, _))
    )

    val c = resultFromFormValidationResult[String](
      typeOfProtectionReferenceValidation(index, columns), createCommitItem(index, InputProtectionTypePage.apply(Event6, _))
    )

    val d = resultFromFormValidationResult[CrystallisedDetails](
      amountCrystallisedAndDateValidation(index, columns, taxYear), createCommitItem(index, AmountCrystallisedAndDatePage.apply(Event6, _))
    )

    Seq(a, b, c, d).combineAll
  }
}
