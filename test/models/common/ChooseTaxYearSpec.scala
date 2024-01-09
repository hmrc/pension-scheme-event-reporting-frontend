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

package models.common

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._

class ChooseTaxYearSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  private val writesTaxYear: Writes[ChooseTaxYear]= ChooseTaxYear.writes(ChooseTaxYear.enumerable(2021))
  private val rdsTaxYear: Reads[ChooseTaxYear] = ChooseTaxYear.reads(ChooseTaxYear.enumerable(2021))

  "ChooseTaxYear" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ChooseTaxYear.values(2021).toSeq)

      forAll(gen) {
        chooseTaxYear =>

          JsString(chooseTaxYear.toString).validate[ChooseTaxYear](rdsTaxYear).asOpt.value mustEqual chooseTaxYear
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ChooseTaxYear.values(2021).map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ChooseTaxYear](rdsTaxYear) mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ChooseTaxYear.values(2021).toSeq)

      forAll(gen) {
        chooseTaxYear =>

          Json.toJson(chooseTaxYear)(writesTaxYear) mustEqual JsString(chooseTaxYear.toString)
      }
    }
  }

  "values" - {
    "yield seq of tax year start years for all years up to 2021 (the hardcoded tax year chosen from the outset" +
      "for this test)" in {
        val expectedResult = (2013 to 2021).reverse.map(yr => ChooseTaxYear(yr.toString))
        ChooseTaxYear.values(2021) mustBe expectedResult
    }
  }
}
