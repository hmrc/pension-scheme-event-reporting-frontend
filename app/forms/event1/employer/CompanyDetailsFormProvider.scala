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

  def apply(): Form[CompanyDetails] =
    Form(
      mapping(
        "companyName" -> text("companyDetails.error.required")
        .verifying(maxLength(100, "companyDetails.error.length")),
      "companyNumber" -> text("companyDetails.error.required")
        .verifying(maxLength(100, "companyDetails.error.length"))
      )(CompanyDetails.apply)(CompanyDetails.unapply)
    )
}
