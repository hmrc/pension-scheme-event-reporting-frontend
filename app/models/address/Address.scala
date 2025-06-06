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

package models.address

import play.api.libs.json._
import utils.CountryOptions

case class Address(addressLine1: String,
                   addressLine2: Option[String],
                   townOrCity: String,
                   county: Option[String],
                   postcode: Option[String],
                   country: String) {

  def lines(countryOptions: CountryOptions): Seq[String] = {
    Seq(
      Some(this.addressLine1),
      this.addressLine2,
      Some(this.townOrCity),
      this.county,
      this.postcode,
      Some(countryOptions.getCountryNameFromCode(this))
    ).flatten(s => s)
  }
  def toTolerantAddress: TolerantAddress = {
    TolerantAddress(Some(addressLine1), addressLine2, Some(townOrCity), county, postcode, Some(country))
  }
}

object Address {
  implicit val formatsAddress: Format[Address] = Json.format[Address]
}
