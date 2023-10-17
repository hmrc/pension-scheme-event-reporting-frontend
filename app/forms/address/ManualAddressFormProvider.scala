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

import forms.address.ManualAddressFormProvider._
import forms.mappings.AddressMapping
import models.address.Address
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.CountryOptions

import javax.inject.Inject

class ManualAddressFormProvider @Inject()(countryOptions: CountryOptions) extends AddressMapping {
  def apply(companyName: String)(implicit messages: Messages): Form[Address] =
    Form(
      mapping(
        addressLines._1 -> addressLineMapping(requiredAddressLineArgs(addressLines._1, companyName)),
        addressLines._2 -> addressLineMapping(requiredAddressLineArgs(addressLines._2, companyName)),
        addressLines._3 -> optionalAddressLineMapping(optionalAddressLineArgs(addressLines._3, companyName)),
        addressLines._4 -> optionalAddressLineMapping(optionalAddressLineArgs(addressLines._4, companyName)),
        postCodeAndCountry._1 -> postCodeWithCountryMapping(postCodeArgs(postCodeAndCountry._1, companyName)),
        postCodeAndCountry._2 -> countryMapping(countryOptions, countryArgs(postCodeAndCountry._2, companyName))
      )(Address.apply)(Address.unapply)
    )
}

object ManualAddressFormProvider {
  private val addressLines: (String, String, String, String) = ("addressLine1", "addressLine2", "addressLine3", "addressLine4")
  private val postCodeAndCountry: (String, String) = ("postCode", "country")
  private val errMsg: (String, String) => String = (key: String, err: String) => s"address.$key.error.$err"
  private def requiredAddressLineArgs(key: String, companyName: String)(implicit messages: Messages): (String, String, String) =
    (messages(errMsg(key, "required"), companyName), messages(errMsg(key, "length"), companyName), messages(errMsg(key, "invalid"), companyName))
  private def optionalAddressLineArgs(key: String, companyName: String)(implicit messages: Messages): (String, String) =
    (messages(errMsg(key, "length"), companyName), messages(errMsg(key, "invalid"), companyName))
  private def postCodeArgs(key: String, companyName: String)(implicit messages: Messages): (String, String, String) =
    (messages(errMsg(key, "required"), companyName), messages(errMsg(key, "invalid"), companyName), "enterPostcode.error.nonUKLength")
  private def countryArgs(key: String, companyName: String)(implicit messages: Messages): (String, String) =
    (messages(errMsg(key, "required"), companyName), errMsg(key, "invalid"))

}
