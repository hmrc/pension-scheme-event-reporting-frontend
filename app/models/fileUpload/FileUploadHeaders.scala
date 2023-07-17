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

package models.fileUpload

object FileUploadHeaders {

  val valueFormField = "value"

  object MemberDetailsFieldNames {
    val firstName = "firstName"
    val lastName = "lastName"
    val nino = "nino"
  }

  object Event1FieldNames {
    val paymentAmount: String = "paymentValue"
    val paymentDate: String = "paymentDate"
    val dateOfEventDay: String = "paymentDate.day"
    val dateOfEventMonth: String = "paymentDate.month"
    val dateOfEventYear: String = "paymentDate.year"
  }

  object Event6FieldNames {
    val typeOfProtection: String = "typeOfProtection"
    val typeOfProtectionReference: String = "typeOfProtectionReference"
    val lumpSumAmount: String = "amountCrystallised"
    val lumpSumDate: String = "crystallisedDate"
    val dateOfEventDay: String = "crystallisedDate.day"
    val dateOfEventMonth: String = "crystallisedDate.month"
    val dateOfEventYear: String = "crystallisedDate.year"
  }

  object Event22FieldNames {
    val taxYear: String = "taxYear"
    val totalAmounts: String = "totalAmounts"
  }

  object Event23FieldNames {
    val taxYear: String = "taxYear"
    val totalAmounts: String = "totalAmounts"
  }
}
