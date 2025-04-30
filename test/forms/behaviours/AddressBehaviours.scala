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

package forms.behaviours

import forms.FormSpec
import forms.mappings.{AddressMapping, Constraints}
import org.scalacheck.Gen
import play.api.data.{Form, FormError}
import utils.CountryOptions
import wolfendale.scalacheck.regexp.RegexpGen

trait AddressBehaviours extends FormSpec with StringFieldBehaviours with Constraints with AddressMapping {

  def formWithAddressField(
                            form: Form[?],
                            fieldName: String,
                            keyAddressRequired: String,
                            keyAddressLength: String,
                            keyAddressInvalid: String): Unit = {

    s"behave like a form with required address field $fieldName" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        RegexpGen.from(regexAddressLine)
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = maxAddressLineLength,
        lengthError = FormError(fieldName, keyAddressLength, Seq(maxAddressLineLength))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, keyAddressRequired)
      )

      behave like fieldWithRegex(
        form,
        fieldName,
        "Apt [12]",
        FormError(fieldName, keyAddressInvalid, Seq(regexAddressLine))
      )

    }
  }

  def formWithOptionalAddressField[T](
                                       form: Form[T],
                                       fieldName: String,
                                       keyAddressLength: String,
                                       keyAddressInvalid: String,
                                       validData: Map[String, String],
                                       accessor: T => Option[String]
                                     ): Unit = {

    s"behave like a form with optional address field $fieldName" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        RegexpGen.from(regexAddressLine)
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = maxAddressLineLength,
        lengthError = FormError(fieldName, keyAddressLength, Seq(maxAddressLineLength))
      )

      behave like fieldWithRegex(
        form,
        fieldName,
        "Apt [12]",
        FormError(fieldName, keyAddressInvalid, Seq(regexAddressLine))
      )

      behave like optionalField(
        form,
        fieldName,
        validData,
        accessor
      )

    }

  }

  def formWithPostCode(form: Form[?], fieldName: String, keyRequired: String, keyLength: String, keyInvalid: String): Unit = {

    "behave like a form with a Post Code" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        RegexpGen.from(regexPostcode)
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = maxPostCodeLength,
        lengthError = FormError(fieldName, keyLength, Seq(maxPostCodeLength))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, keyRequired)
      )

      behave like fieldWithRegex(
        form,
        fieldName,
        "12AB AB1",
        FormError(fieldName, keyInvalid, Seq(regexPostcode))
      )

      "transform the Post Code value correctly" in {
        val postCode = "  zz11zz  "
        val result = form.bind(Map(fieldName -> postCode))
        result.errors.size mustBe 0
        result.get mustBe "ZZ1 1ZZ"
      }
    }

  }

  def formWithCountryAndPostCode[T](
                                     form: Form[T],
                                     keyRequired: String,
                                     keyInvalid: String,
                                     keyNonUKLength: String,
                                     validOtherData: Map[String, String],
                                     getPostCode: T => String): Unit = {

    "behave like a form with Post Code and Country" - {

      "bind successfully when country is non UK and postcode is non-UK postal format" in {
        val result = form.bind(validOtherData ++ Map("country" -> "IN", "postCode" -> "123"))
        getPostCode(result.get) mustBe "123"
      }

      "bind successfully when country is UK and postcode is of correct format" in {
        val result = form.bind(validOtherData ++ Map("country" -> "GB", "postCode" -> "AB1 1AB"))
        getPostCode(result.get) mustBe "AB1 1AB"
      }

      "fail to bind when country is UK and postCode is not provided" in {
        val result = form.bind(validOtherData ++ Map("country" -> "GB", "postCode" -> ""))

        result.errors mustBe Seq(FormError("postCode", keyRequired))
      }

      "fail to bind when country is NON UK and postCode is more than 10 characters" in {
        val result = form.bind(validOtherData ++ Map("country" -> "IN", "postCode" -> "12345678911"))

        result.errors mustBe Seq(FormError("postCode", keyNonUKLength))
      }

      Seq("A 1223", "1234 A23", "AA1 BBB", "AA 8989").foreach { testPostCode =>
        s"fail to bind when postCode $testPostCode is not valid" in {

          val result = form.bind(validOtherData ++ Map("country" -> "GB", "postCode" -> testPostCode))

          result.errors mustBe Seq(FormError("postCode", keyInvalid))

        }
      }

      "transform the Post Code value correctly" in {
        val postCode = "  zz11zz  "
        val result = form.bind(validOtherData ++ Map("postCode" -> postCode, "country" -> "GB"))
        result.errors.size mustBe 0
        getPostCode(result.get) mustBe "ZZ1 1ZZ"
      }
    }

  }

  def formWithCountry(
                       form: Form[?],
                       fieldName: String,
                       keyRequired: String,
                       keyInvalid: String,
                       countryOptions: CountryOptions,
                       validOtherData: Map[String, String]): Unit = {

    "behave like a form with a Country field" - {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        countryCodeGenerator(countryOptions)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, keyRequired)
      )

      "fail to bind when the country code is not valid" in {
        val result = form.bind(validOtherData ++ Map(fieldName -> "XXX"))
        result.errors mustBe Seq(FormError(fieldName, keyInvalid))
      }
    }

  }

  private def countryCodeGenerator(countryOptions: CountryOptions): Gen[String] = {
    Gen.oneOf(countryOptions.options.map(_.value))
  }

}
