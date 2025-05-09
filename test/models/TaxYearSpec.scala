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

package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}
import utils.DateHelper

import java.time.LocalDate

class TaxYearSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "TaxYear" - {
    DateHelper.setDate(Some(LocalDate.now()))
    TaxYear.values.foreach { taxYear =>
      s"must deserialise valid $taxYear values" in {
          val ty = JsString(taxYear.startYear).validate[TaxYear]
          ty.asOpt.value mustEqual taxYear
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TaxYear.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[TaxYear] mustEqual JsError("error.invalid")
      }
    }

    TaxYear.values.foreach { taxYear =>
      s"must serialise $taxYear" in {
          Json.toJson(taxYear) mustEqual JsString(taxYear.startYear)
      }
    }
  }

  "Values" - {
    "must return a sequence of 7 year ranges from 2024-2025 to 2018-2019" in {
      DateHelper.setDate(Some(LocalDate.of(2024, 6, 1)))
      TaxYear.values mustEqual
        Seq(
          TaxYear("2024"), TaxYear("2023"), TaxYear( "2022"), TaxYear( "2021"), TaxYear( "2020"), TaxYear( "2019"), TaxYear( "2018"))
    }
  }
}
