/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.event1.employer

import forms.mappings.Mappings
import models.event1.employer.CompanyDetails
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Inject

class CompanyDetailsFormProvider @Inject() extends Mappings {

  private val companyNameLength: Int = 160
  private val companyNumberMinLength: Int = 6
  private val companyNumberMaxLength: Int = 8

  def apply(): Form[CompanyDetails] = Form(
    mapping(
      "companyName" -> text("companyDetails.companyName.error.required")
        .verifying(
          firstError(
            maxLength(
              companyNameLength,
              "companyDetails.companyName.error.length"
            ),
            safeText("companyDetails.companyName.error.invalidCharacters")
          )
        ),
      "companyNumber" -> text("companyDetails.companyNumber.error.required")
        .verifying(
          firstError(
            maxLength(companyNumberMaxLength, "companyDetails.companyNumber.error.length"),
            minLength(companyNumberMinLength, "companyDetails.companyNumber.error.length"),
            safeText("companyDetails.companyNumber.error.invalidCharacters")
          )
        )
    )(CompanyDetails.apply)(CompanyDetails.unapply)
  )


}
