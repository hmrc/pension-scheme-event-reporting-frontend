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

package forms.event24

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class TypeOfProtectionGroup2ReferenceFormProvider @Inject() extends Mappings {

  val minLength: Int = 8
  val maxLength: Int = 15

  def apply(): Form[String] =
    Form(
      "value" -> text("typeOfProtectionReference.error.required")
        .verifying(
          firstError(
            maxLength(maxLength, "typeOfProtectionReference.event24.error.maxLength"),
            minLength(minLength, "typeOfProtectionReference.event24.error.minLength"),
            regexp(protectionReferenceRegex, "typeOfProtectionReference.error.invalid"))
        )
    )
}