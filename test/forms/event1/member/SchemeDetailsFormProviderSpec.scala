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

package forms.event1.member

import forms.SchemeDetails.SchemeDetailsFormProvider
import forms.behaviours.StringFieldBehaviours

class SchemeDetailsFormProviderSpec extends StringFieldBehaviours {

  private val maxLength = 150

  private val form = new SchemeDetailsFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    "bind empty data" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.value.value mustBe ""
      result.errors mustBe empty
    }

  }
}
