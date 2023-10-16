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

import forms.address.ManualAddressFormProvider.addressLineStr
import forms.mappings.AddressMapping
import models.address.Address
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.CountryOptions

import javax.inject.Inject

// TODO: refactor
class ManualAddressFormProvider @Inject()(countryOptions: CountryOptions) extends AddressMapping {
  //scalastyle:off
  def apply(companyName: String)(implicit messages: Messages): Form[Address] = Form(
    mapping(
      // TODO: refactor all below to reuse common component.
      addressLineStr(1) ->
        addressLineMapping(
          messages(s"address.${addressLineStr(1)}.error.required", companyName),
          messages(s"address.${addressLineStr(1)}.error.length", companyName),
          messages(s"address.${addressLineStr(1)}.error.invalid", companyName)
        ),
      addressLineStr(2) ->
        addressLineMapping(
          messages(s"address.${addressLineStr(2)}.error.required", companyName),
          messages(s"address.${addressLineStr(2)}.error.length", companyName),
          messages(s"address.${addressLineStr(2)}.error.invalid", companyName)
        ),
      addressLineStr(3) ->
        optionalAddressLineMapping(
          messages("address.addressLine3.error.length", companyName),
          messages("address.addressLine3.error.invalid", companyName)
        ),
      addressLineStr(4) ->
        optionalAddressLineMapping(
          messages("address.addressLine4.error.length", companyName),
          messages("address.addressLine4.error.invalid", companyName)
        ),
      "postCode" ->
        postCodeWithCountryMapping(
          messages("address.postCode.error.required", companyName),
          messages("enterPostcode.error.invalid", companyName),
          "enterPostcode.error.nonUKLength"
        ),
      "country" ->
        countryMapping
        (countryOptions,
          messages("address.country.error.required", companyName),
          "address.country.error.invalid"
        )
    )(Address.apply)(Address.unapply)
  )
}

object ManualAddressFormProvider {
  val addressLineStr: Int => String = (int: Int) => s"addressLine$int"
}
