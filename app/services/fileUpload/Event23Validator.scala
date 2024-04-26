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
import forms.common.{ChooseTaxYearFormProvider, MembersDetailsFormProvider, TotalPensionAmountsFormProvider}
import models.common.ChooseTaxYear
import models.enumeration.EventType
import models.enumeration.EventType.Event23
import models.fileUpload.FileUploadHeaders.Event23FieldNames.totalAmounts
import models.fileUpload.FileUploadHeaders.valueFormField
import pages.common.{ChooseTaxYearPage, MembersDetailsPage, TotalPensionAmountsPage}
import play.api.data.Form
import play.api.i18n.Messages
import services.fileUpload.Validator.Result

class Event23Validator @Inject()(
                                  membersDetailsFormProvider: MembersDetailsFormProvider,
                                  chooseTaxYearFormProvider: ChooseTaxYearFormProvider,
                                  totalPensionAmountsFormProvider: TotalPensionAmountsFormProvider,
                                  config: FrontendAppConfig
                                ) extends Validator {
  override val eventType: EventType = EventType.Event23

  override protected def validHeader: String = config.validEvent23Header

  private val fieldNoTaxYear = 3
  private val fieldNoTotalAmounts = 4

  private def taxYearValidation(index: Int,
                                chargeFields: Seq[String],
                                taxYear: Int): Validated[Seq[ValidationError], ChooseTaxYear] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTaxYear).takeWhile(_ != ' '), models.fileUpload.FileUploadHeaders.Event23FieldNames.taxYear, fieldNoTaxYear)
    )

    val form: Form[ChooseTaxYear] = chooseTaxYearFormProvider(eventType = Event23, maxTaxYear = taxYear)
    form.bind(
      Field.seqToMap(fields)
    ).fold(
      formWithErrors =>
        Invalid(errorsFromForm(formWithErrors, fields, index)),
      value =>
        Valid(value)
    )
  }

  private def totalAmountsValidation(index: Int,
                                     chargeFields: Seq[String]): Validated[Seq[ValidationError], BigDecimal] = {
    val fields = Seq(
      Field(valueFormField, chargeFields(fieldNoTotalAmounts), totalAmounts, fieldNoTotalAmounts)
    )
    val form: Form[BigDecimal] = totalPensionAmountsFormProvider()
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
      memberDetailsValidation(index, columns, membersDetailsFormProvider(Event23, index)),
      createCommitItem(index, MembersDetailsPage.apply(Event23, _))
    )

    val b = resultFromFormValidationResult[ChooseTaxYear](
      taxYearValidation(index, columns, taxYear), createCommitItem(index, ChooseTaxYearPage.apply(Event23, _)
      )(ChooseTaxYear.writes(ChooseTaxYear.enumerable(taxYear)))
    )

    val c = resultFromFormValidationResult[BigDecimal](
      totalAmountsValidation(index, columns), createCommitItem(index, TotalPensionAmountsPage.apply(Event23, _))
    )
    Seq(a, b, c).combineAll
  }
}
