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

package forms

import forms.mappings.{Mappings, Transforms}
import play.api.data.Form

import javax.inject.Inject

class DeclarationPspFormProvider @Inject() extends Mappings with Transforms{

  def apply(authorisingPSAID: Option[String]): Form[String] =
    Form(
      "value" -> text("pspDeclaration.error.required")
        .transform(authorisingPSAIDWhitespaceRemoval, noTransform)
        .verifying(
          firstError(
            minLength(8, "pspDeclaration.error.length"),
            maxLength(8, "pspDeclaration.error.length"),
            regexp(psaIdRegex, "pspDeclaration.error.length"),
            isEqual(authorisingPSAID, "pspDeclaration.error.noMatch")
          )
        )
    )

  private def authorisingPSAIDWhitespaceRemoval(value: String): String = {
    value.filterNot(_.isWhitespace)
  }
}
