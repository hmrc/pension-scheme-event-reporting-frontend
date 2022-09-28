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

package forms.event1

import forms.event1.PersonNameFormProvider.{firstNameLength, lastNameLength}
import forms.mappings.{Mappings, Transforms}
import models.event1.MembersDetails
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Inject

class MembersDetailsFormProvider @Inject() extends Mappings with Transforms {

  def apply(): Form[MembersDetails] =
    Form(
      mapping("firstName" ->
        text("membersDetails.error.firstName.required").verifying(
          firstError(
            maxLength(firstNameLength, "membersDetails.error.firstName.length"),
            regexp(regexName, "membersDetails.error.firstName.invalid"))),
        "lastName" ->
          text("membersDetails.error.lastName.required").verifying(
            firstError(
              maxLength(lastNameLength, "membersDetails.error.lastName.length"),
              regexp(regexSurname, "membersDetails.error.lastName.invalid"))
          ),
        "nino" ->
          text("membersDetails.error.nino.required")
            .transform(noSpaceWithUpperCaseTransform, noTransform)
            .verifying(
              validNino("membersDetails.error.nino.invalid")
            ))(MembersDetails.apply)(MembersDetails.unapply)
    )

}

object PersonNameFormProvider {
  val firstNameLength: Int = 35
  val lastNameLength: Int = 35
}