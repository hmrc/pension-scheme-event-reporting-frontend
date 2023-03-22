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

import forms.common.PersonNameFormProvider.{firstNameLength, lastNameLength}
import forms.mappings.{Mappings, Transforms}
import models.common.MembersDetails
import models.enumeration.EventType
import models.enumeration.EventType.Event2
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Inject

class MembersDetailsFormProvider @Inject() extends Mappings with Transforms {

  def apply(eventType: EventType): Form[MembersDetails] = {
    val memberType = eventType match {
      case Event2 => "deceasedMembersDetails"
      case _ => "membersDetails"
    }
    Form(
      mapping("firstName" ->
        text(s"${memberType}.error.firstName.required").verifying(
          firstError(
            maxLength(firstNameLength, s"${memberType}.error.firstName.length"),
            regexp(regexName, s"${memberType}.error.firstName.invalid"))),
        "lastName" ->
          text(s"${memberType}.error.lastName.required").verifying(
            firstError(
              maxLength(lastNameLength, s"${memberType}.error.lastName.length"),
              regexp(regexName, s"${memberType}.error.lastName.invalid"))
          ),
        "nino" ->
          text(s"${memberType}.error.nino.required")
            .transform(noSpaceWithUpperCaseTransform, noTransform)
            .verifying(
              validNino(s"${memberType}.error.nino.invalid")
            ))(MembersDetails.apply)(MembersDetails.unapply)
    )
  }

}

object PersonNameFormProvider {
  val firstNameLength: Int = 35
  val lastNameLength: Int = 35
}