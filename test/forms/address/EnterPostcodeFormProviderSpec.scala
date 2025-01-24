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

package forms.address

import base.SpecBase
import data.SampleData.companyDetails
import forms.behaviours.AddressBehaviours

class EnterPostcodeFormProviderSpec extends SpecBase with AddressBehaviours {

  private val companyName = companyDetails.companyName

  private val requiredKey = s"Enter $companyName’s postcode"
  private val lengthKey = "enterPostcode.error.length"
  private val invalid = "Enter a full UK postcode"
  private val fieldName = "value"

  private val formProvider = new EnterPostcodeFormProvider()
  private val form = formProvider(companyName)

  ".value" - {
    behave like formWithPostCode(
      form,
      fieldName,
      requiredKey,
      lengthKey,
      invalid
    )
  }
}
