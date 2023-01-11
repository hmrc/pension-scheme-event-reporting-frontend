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

package forms.address

import forms.mappings.AddressMapping
import models.address.Address
import play.api.data.Form
import play.api.data.Forms.mapping
import utils.CountryOptions

import javax.inject.Inject

class ManualAddressFormProvider @Inject()(countryOptions: CountryOptions) extends AddressMapping {

  def apply(): Form[Address] = Form(
    mapping(
      "addressLine1" ->
        addressLineMapping("address.addressLine1.error.required", "address.addressLine1.error.length",
          "address.addressLine1.error.invalid"),
      "addressLine2" ->
        addressLineMapping("address.addressLine2.error.required", "address.addressLine2.error.length",
          "address.addressLine2.error.invalid"),
      "addressLine3" ->
        optionalAddressLineMapping("address.addressLine3.error.length", "address.addressLine3.error.invalid"),
      "addressLine4" ->
        optionalAddressLineMapping("address.addressLine4.error.length", "address.addressLine4.error.invalid"),
      "postCode" ->
        postCodeWithCountryMapping("address.postCode.error.required", "enterPostcode.error.invalid",
          "enterPostcode.error.nonUKLength"),
      "country" ->
        countryMapping(countryOptions, "address.country.error.required", "address.country.error.invalid")
    )(Address.apply)(Address.unapply)
  )
}
