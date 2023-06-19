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
import forms.common.{ChooseTaxYearFormProvider, MembersDetailsFormProvider, TotalPensionAmountsFormProvider}
import models.common.{ChooseTaxYear, MembersDetails}
import models.enumeration.EventType
import models.enumeration.EventType.Event22
import models.fileUpload.FileUploadHeaders.Event22FieldNames.{taxYear, totalAmounts}
import pages.common.{ChooseTaxYearPage, MembersDetailsPage, TotalPensionAmountsPage}
import play.api.data.Form
import play.api.i18n.Messages
import services.fileUpload.Validator.Result

import java.time.LocalDate

class Event22Validator @Inject()(
                                  membersDetailsFormProvider: MembersDetailsFormProvider,
                                  chooseTaxYearFormProvider: ChooseTaxYearFormProvider,
                                  totalPensionAmountsFormProvider: TotalPensionAmountsFormProvider,
                                  config: FrontendAppConfig
                                ) extends Validator {
  override val eventType: EventType = EventType.Event22

  override protected def validHeader: String = config.validEvent22Header

  private val fieldNoFirstName = 1
  private val fieldNoLastName = 2
  private val fieldNoNino = 3
  private val fieldNoTaxYear = 4
  private val fieldNoTotalAmounts = 5

  private def taxYearValidation(index: Int,
                                chargeFields: Seq[String])(implicit messages: Messages): Validated[Seq[ValidationError], ChooseTaxYear] = {

    val fields = Seq(
      Field(taxYear, chargeFields(fieldNoTaxYear), taxYear, fieldNoTaxYear)
    )
    val form: Form[ChooseTaxYear] = chooseTaxYearFormProvider(eventType = Event22)
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  private def totalAmountsValidation(index: Int,
                                      chargeFields: Seq[String])(implicit messages: Messages): Validated[Seq[ValidationError], BigDecimal] = {
    val fields = Seq(
      Field(totalAmounts, chargeFields(fieldNoTotalAmounts), totalAmounts, fieldNoTotalAmounts)
    )
    val form: Form[BigDecimal] = totalPensionAmountsFormProvider()
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors => Invalid(errorsFromForm(formWithErrors, fields, index)),
      value => Valid(value)
    )
  }

  override protected def validateFields(startDate: LocalDate,
                                        index: Int,
                                        columns: Seq[String])(implicit messages: Messages): Result = {
    val a = resultFromFormValidationResult[MembersDetails](
      memberDetailsValidation(index, columns, membersDetailsFormProvider(Event22, index)),
      createCommitItem(index, MembersDetailsPage.apply(Event22, _))
    )

    val b = resultFromFormValidationResult[ChooseTaxYear](
      taxYearValidation(index, columns), createCommitItem(index, ChooseTaxYearPage.apply(Event22, _))
    )

    val c = resultFromFormValidationResult[BigDecimal](
      totalAmountsValidation(index, columns), createCommitItem(index, TotalPensionAmountsPage.apply(Event22, _))
    )
    Seq(a, b, c).combineAll
  }
}
