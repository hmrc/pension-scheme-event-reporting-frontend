package models

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class TestCheckBoxSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "TestCheckBox" - {

    "must deserialise valid values" in {

      val gen = arbitrary[TestCheckBox]

      forAll(gen) {
        testCheckBox =>

          JsString(testCheckBox.toString).validate[TestCheckBox].asOpt.value mustEqual testCheckBox
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TestCheckBox.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[TestCheckBox] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[TestCheckBox]

      forAll(gen) {
        testCheckBox =>

          Json.toJson(testCheckBox) mustEqual JsString(testCheckBox.toString)
      }
    }
  }
}
