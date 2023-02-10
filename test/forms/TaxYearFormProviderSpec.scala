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

///*
// * Copyright 2023 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */

package forms

import forms.behaviours.OptionFieldBehaviours
import models.TaxYear
import org.scalatest.BeforeAndAfterEach
import play.api.data.FormError
import utils.DateHelper

import java.time.LocalDate

class TaxYearFormProviderSpec extends OptionFieldBehaviours with BeforeAndAfterEach {

  override def beforeEach: Unit = {
    super.beforeEach
    DateHelper.setDate(Some(LocalDate.of(2023, 2, 10)))
  }
  private val form = new TaxYearFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "taxYear.error.required"

    behave like optionsField[String](
      form,
      fieldName,
      validValues  = TaxYear.values.map(_.startYear),
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
