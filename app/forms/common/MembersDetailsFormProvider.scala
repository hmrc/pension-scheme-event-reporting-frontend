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

package forms.common

import forms.common.MembersDetailsFormProvider.{firstName, lastName, nameMapping, ninoMapping}
import forms.mappings.{Mappings, Transforms}
import models.common.MembersDetails
import models.enumeration.EventType
import models.enumeration.EventType.Event2
import play.api.data.Forms.mapping
import play.api.data.validation.Constraint
import play.api.data.{Form, Mapping}

class MembersDetailsFormProvider {

  def apply(eventType: EventType, memberPageNo: Int = 0): Form[MembersDetails] = {

    val detailsType = (eventType, memberPageNo) match {
      case (Event2, 1) => "deceasedMembersDetails"
      case (Event2, 2) => "beneficiaryDetails"
      case _ => "membersDetails"
    }

    Form(
      mapping(
        nameMapping(firstName, detailsType),
        nameMapping(lastName, detailsType),
        ninoMapping(detailsType)
      )(MembersDetails.apply)(m => Some(Tuple.fromProductTyped(m)))
    )
  }
}

object MembersDetailsFormProvider extends Mappings with Transforms {

  private val (firstName: String, lastName: String, nino: String) = ("firstName", "lastName", "nino")
  val maximumNameLength: Int = 35

  private val nameMapping: (String, String) => (String, Mapping[String]) = (field: String, detailsType: String) =>
    field -> text(s"$detailsType.error.$field.required")
      .verifying(nameIsValid(field, detailsType))

  private val ninoMapping: String => (String, Mapping[String]) = (detailsType: String) =>
    nino -> text(s"$detailsType.error.$nino.required").transform(noSpaceWithUpperCaseTransform, noTransform)
      .verifying(ninoIsValid(detailsType))

  private val nameIsValid: (String, String) => Constraint[String] = (nameField: String, detailsType: String) =>
    firstError(
      maxLength(maximumNameLength, s"$detailsType.error.$nameField.length"),
      regexp(regexName, s"$detailsType.error.$nameField.invalid")
    )

  private val ninoIsValid: String => Constraint[String] = (_: String) => validNino
}
