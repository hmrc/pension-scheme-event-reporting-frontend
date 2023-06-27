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

package forms.common

import forms.behaviours.OptionFieldBehaviours
import models.common.ChooseTaxYear
import models.enumeration.EventType.{Event22, Event23}
import org.scalatest.BeforeAndAfterEach
import play.api.data.FormError

import java.time.LocalDate

class ChooseTaxYearFormProviderSpec extends OptionFieldBehaviours with BeforeAndAfterEach{

  override protected def beforeEach(): Unit = {
    utils.DateHelper.setDate(Some(LocalDate.of(2021, 6, 1)))
  }

  ".value" - {

    "event23" - {
      val form = new ChooseTaxYearFormProvider()(Event23, 2021)
      val fieldName = "value"
      val requiredKeyEvent23 = "chooseTaxYear.event23.error.required"

      behave like optionsField[ChooseTaxYear](
        form,
        fieldName,
        validValues = ChooseTaxYear.valuesForYearRange(2021),
        invalidError = FormError(key = fieldName, message = "chooseTaxYear.event22.error.outsideRange", args = Seq("2013", "2021"))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKeyEvent23, Seq("2013", "2021"))
      )
    }

    "event22" - {
      val form = new ChooseTaxYearFormProvider()(Event22, 2021)
      val fieldName = "value"
      val requiredKeyEvent22 = "chooseTaxYear.event22.error.required"

      behave like optionsField[ChooseTaxYear](
        form,
        fieldName,
        validValues = ChooseTaxYear.valuesForYearRange(2021),
        invalidError = FormError(fieldName, "chooseTaxYear.event22.error.outsideRange", Seq("2013", "2021"))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKeyEvent22, Seq("2013", "2021"))
      )

      "not bind when year is earlier than min" in {
        val fieldName = "value"
        val fields = Map[String, String](
          fieldName -> "2012"
        )
        val result = form.bind(fields).apply(fieldName)
        result.errors mustEqual Seq(FormError(fieldName, "chooseTaxYear.event22.error.outsideRange", Seq("2013", "2021")))
      }

      "not bind when year is greater than max" in {
        utils.DateHelper.setDate(Some(LocalDate.of(2021,6,1)))
        val fieldName = "value"
        val fields = Map[String, String](
          fieldName -> "2024"
        )
        val result = form.bind(fields).apply(fieldName)
        result.errors mustEqual Seq(FormError(fieldName, "chooseTaxYear.event22.error.outsideRange", Seq("2013", "2021")))
      }
    }
  }


}
