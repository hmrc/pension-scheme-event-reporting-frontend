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

package forms.address

import forms.address.ManualAddressFormProvider._
import forms.mappings.AddressMapping
import models.address.Address
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.CountryOptions

import javax.inject.Inject

class ManualAddressFormProvider @Inject()(countryOptions: CountryOptions) extends AddressMapping {
  def apply(name: String)(implicit messages: Messages): Form[Address] =
    Form(
      mapping(
        addressLines._1 -> addressLineMapping(requiredAddressLineArgs(addressLines._1, name)),
        addressLines._2 -> optionalAddressLineMapping(optionalAddressLineArgs(addressLines._2, name)),
        addressLines._3 -> addressLineMapping(requiredAddressLineArgs(addressLines._3, name)),
        addressLines._4 -> optionalAddressLineMapping(optionalAddressLineArgs(addressLines._4, name)),
        postCodeAndCountry._1 -> postCodeWithCountryMapping(postCodeArgs(postCodeAndCountry._1, name)),
        postCodeAndCountry._2 -> countryMapping(countryOptions, countryArgs(postCodeAndCountry._2, name))
      )(Address.apply)(Address.unapply)
    )
}

object ManualAddressFormProvider {
  private val addressLines: (String, String, String, String) = ("addressLine1", "addressLine2", "townOrCity", "county")
  private val postCodeAndCountry: (String, String) = ("postCode", "country")
  private val errMsg: (String, String) => String = (key: String, err: String) => s"address.$key.error.$err"
  private def requiredAddressLineArgs(key: String, name: String)(implicit messages: Messages): (String, String, String) =
    (messages(errMsg(key, "required"), name), messages(errMsg(key, "length"), name), messages(errMsg(key, "invalid"), name))
  private def optionalAddressLineArgs(key: String, name: String)(implicit messages: Messages): (String, String) =
    (messages(errMsg(key, "length"), name), messages(errMsg(key, "invalid"), name))
  private def postCodeArgs(key: String, name: String)(implicit messages: Messages): (String, String, String) =
    (messages(errMsg(key, "required"), name), messages(errMsg(key, "invalid"), name), "enterPostcode.error.nonUKLength")
  private def countryArgs(key: String, name: String)(implicit messages: Messages): (String, String) =
    (messages(errMsg(key, "required"), name), errMsg(key, "invalid"))

}
