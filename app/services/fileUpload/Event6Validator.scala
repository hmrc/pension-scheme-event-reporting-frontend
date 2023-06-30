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
import forms.event6.{AmountCrystallisedAndDateFormProvider, InputProtectionTypeFormProvider, TypeOfProtectionFormProvider}
import models.common.MembersDetails
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event6}
import models.event6.{CrystallisedDetails, TypeOfProtection}
import models.fileUpload.FileUploadHeaders.Event6FieldNames.{lumpSumAmount, lumpSumDate, typeOfProtection, typeOfProtectionReference}
import models.fileUpload.FileUploadHeaders.valueFormField
import pages.common.MembersDetailsPage
import pages.event6.{AmountCrystallisedAndDatePage, InputProtectionTypePage, TypeOfProtectionPage}
import play.api.data.Form
import play.api.i18n.Messages
import services.fileUpload.Validator.Result

class Event6Validator @Inject()(
                                 membersDetailsFormProvider: MembersDetailsFormProvider,
                                 typeOfProtectionFormProvider: TypeOfProtectionFormProvider,
                                 inputProtectionTypeFormProvider: InputProtectionTypeFormProvider,
                                 amountCrystallisedAndDateFormProvider: AmountCrystallisedAndDateFormProvider,
                                 config: FrontendAppConfig
                               )(implicit messages: Messages) extends Validator {
  override val eventType: EventType = EventType.Event6

  override protected def validHeader: String = config.validEvent6Header

  private val fieldNoTypeOfProtection = 3
  private val fieldNoTypeOfProtectionReference = 4
  private val fieldNoLumpSumAmount = 5
  private val fieldNoLumpSumDate = 6

  private def typeOfProtectionValidation(index: Int,
                                         chargeFields: Seq[String]): Validated[Seq[ValidationError], TypeOfProtection] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTypeOfProtection), typeOfProtection, fieldNoTypeOfProtection)
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

  private def lumpSumAmountValidation(index: Int,
                                      chargeFields: Seq[String]): Validated[Seq[ValidationError], CrystallisedDetails] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoLumpSumAmount), lumpSumAmount, fieldNoLumpSumAmount)
    )
    val form: Form[CrystallisedDetails] = amountCrystallisedAndDateFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def lumpSumDateValidation(index: Int,
                                    chargeFields: Seq[String]): Validated[Seq[ValidationError], CrystallisedDetails] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoLumpSumDate), lumpSumDate, fieldNoLumpSumDate)
    )
    val form: Form[CrystallisedDetails] = amountCrystallisedAndDateFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  override protected def validateFields(index: Int,
                                        columns: Seq[String],
                                        taxYear: Int): Result = {
    val a = resultFromFormValidationResult[MembersDetails](
      memberDetailsValidation(index, columns, membersDetailsFormProvider(Event22, index)),
      createCommitItem(index, MembersDetailsPage.apply(Event22, _))
    )

    val b = resultFromFormValidationResult[TypeOfProtection](
      typeOfProtectionValidation(index, columns), createCommitItem(index, TypeOfProtectionPage.apply(Event6, _))
    )

    val c = resultFromFormValidationResult[String](
      typeOfProtectionReferenceValidation(index, columns), createCommitItem(index, InputProtectionTypePage.apply(Event6, _))
    )

    val d = resultFromFormValidationResult[CrystallisedDetails](
      lumpSumAmountValidation(index, columns), createCommitItem(index, AmountCrystallisedAndDatePage.apply(Event6, _))
    )

    val e = resultFromFormValidationResult[CrystallisedDetails](
      lumpSumDateValidation(index, columns), createCommitItem(index, AmountCrystallisedAndDatePage.apply(Event6, _))
    )

    Seq(a, b, c, d, e).combineAll
  }
}
