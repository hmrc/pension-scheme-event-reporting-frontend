package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class TaxYearSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "TaxYear" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(TaxYear.values.toSeq)

      forAll(gen) {
        taxYear =>

          JsString(taxYear.toString).validate[TaxYear].asOpt.value mustEqual taxYear
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TaxYear.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[TaxYear] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(TaxYear.values.toSeq)

      forAll(gen) {
        taxYear =>

          Json.toJson(taxYear) mustEqual JsString(taxYear.toString)
      }
    }
  }
}
