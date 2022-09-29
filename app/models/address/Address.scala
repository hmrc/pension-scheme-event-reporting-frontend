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

package models.address

import play.api.libs.json._
import utils.CountryOptions

case class AddressRecord(address: Address)

object AddressRecord {
  implicit val addressRecordFormat: Format[AddressRecord] = Json.format[AddressRecord]
}

case class Address(addressLine1: String,
                   addressLine2: String,
                   addressLine3: Option[String],
                   addressLine4: Option[String],
                   postcode: Option[String],
                   country: String) {

  def lines(countryOptions: CountryOptions): Seq[String] = {
    Seq(
      Some(this.addressLine1),
      Some(this.addressLine2),
      this.addressLine3,
      this.addressLine4,
      this.postcode,
      Some(countryOptions.getCountryNameFromCode(this))
    ).flatten(s => s)
  }
}

object Address {
  implicit val formatsAddress: Format[Address] = Json.format[Address]
}
