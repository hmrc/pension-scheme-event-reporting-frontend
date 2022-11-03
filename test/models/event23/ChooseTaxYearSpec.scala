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

package models.event23

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}
import utils.DateHelper

import java.time.LocalDate

class ChooseTaxYearSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  private def genYear: Gen[Int] =
    Gen.oneOf(Seq(2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025, 2026, 2027, 2028))

  "ChooseTaxYear" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ChooseTaxYear.values.toSeq)

      forAll(gen) {
        chooseTaxYear =>

          JsString(chooseTaxYear.toString).validate[ChooseTaxYear].asOpt.value mustEqual chooseTaxYear
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ChooseTaxYear.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ChooseTaxYear] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ChooseTaxYear.values.toSeq)

      forAll(gen) {
        chooseTaxYear =>

          Json.toJson(chooseTaxYear) mustEqual JsString(chooseTaxYear.toString)
      }
    }
  }

  "values" - {
    "yield seq of tax year start years for all years up to BUT NOT INCLUDING current calendar year " +
      "where current calendar date is set to 5th April (end of old tax year) of a random year" in {
      forAll(genYear -> "valid years") { year =>
        DateHelper.setDate(Some(LocalDate.of(year, 4, 5)))
        val expectedResult =
          (2015 until year).reverse.map(yr => ChooseTaxYear(yr.toString))
        ChooseTaxYear.values mustBe expectedResult
      }
    }

    "yield tax year start years for all years up to AND INCLUDING current calendar year " +
      "where current calendar date is set to 6th April (start of new tax year) of a random year" in {
      forAll(genYear -> "valid years") { year =>
        DateHelper.setDate(Some(LocalDate.of(year, 4, 6)))
        val expectedResult =
          (2015 to year).reverse.map(yr => ChooseTaxYear(yr.toString))
        ChooseTaxYear.values mustBe expectedResult
      }
    }
  }
}
