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

import base.SpecBase
import data.SampleData.companyDetails
import forms.behaviours.{AddressBehaviours, FormBehaviours}
import models.address.Address
import utils.FakeCountryOptions
import play.api.data.Form

import scala.util.Random

class ManualAddressFormProviderSpec extends SpecBase with AddressBehaviours with FormBehaviours {
  private def alphaString(max: Int = maxAddressLineLength) =
    Random.alphanumeric take Random.shuffle(Range(1, max).toList).head mkString ""

  private val companyName = companyDetails.companyName
  private val addressLine1 = alphaString()
  private val addressLine2 = alphaString()
  private val townOrCity = alphaString()
  private val county = alphaString()
  private val postCode = "ZZ1 1ZZ"

  private val countryOptions = FakeCountryOptions()

  val validData: Map[String, String] = Map(
    "addressLine1" -> addressLine1,
    "addressLine2" -> addressLine2,
    "townOrCity" -> townOrCity,
    "county" -> county,
    "postCode" -> postCode,
    "country" -> "GB"
  )

  val form: Form[Address] = new ManualAddressFormProvider(countryOptions)(companyName)

  "Address form" - {
    behave like questionForm(Address(
      addressLine1,
      Some(addressLine2),
      townOrCity,
      Some(county),
      Some(postCode),
      "GB"
    ))

    behave like formWithCountry(
      form,
      "country",
      messages("address.country.error.required", companyName),
      "address.country.error.invalid",
      countryOptions,
      Map(
        "addressLine1" -> addressLine1,
        "townOrCity" -> townOrCity
      )
    )

    behave like formWithCountryAndPostCode(
      form,
      messages("address.postCode.error.required", companyName),
      messages("address.postCode.error.invalid"),
      "enterPostcode.error.nonUKLength",
      Map(
        "addressLine1" -> addressLine1,
        "townOrCity" -> townOrCity
      ),
      (address: Address) => address.postcode.getOrElse("")
    )


    behave like formWithAddressField(
      form,
      "addressLine1",
      messages("address.addressLine1.error.required", companyName),
      messages("address.addressLine1.error.length", companyName),
      messages("address.addressLine1.error.invalid", companyName)
    )

    behave like formWithAddressField(
      form,
      "townOrCity",
      messages("address.townOrCity.error.required", companyName),
      messages("address.townOrCity.error.length", companyName),
      messages("address.townOrCity.error.invalid", companyName)
    )

    behave like formWithOptionalAddressField(
      form,
      "addressLine2",
      messages("address.addressLine2.error.length", companyName),
      messages("address.addressLine2.error.invalid", companyName),
      validData,
      (address: Address) => address.addressLine2
    )

    behave like formWithOptionalAddressField(
      form,
      "county",
      messages("address.county.error.length", companyName),
      messages("address.county.error.invalid", companyName),
      validData,
      (address: Address) => address.county
    )
  }

}
