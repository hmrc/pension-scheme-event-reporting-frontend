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

package forms.address

import forms.mappings.AddressMapping
import play.api.data.Form
import play.api.i18n.Messages

import javax.inject.Inject

class EnterPostcodeFormProvider @Inject() extends AddressMapping {
  def apply(companyName: String)(implicit messages: Messages): Form[String] =
    Form(
      "value" -> postCodeMapping(
        messages("enterPostcode.error.required", companyName),
        "enterPostcode.error.length",
        "enterPostcode.error.invalid"
      )
    )
}
