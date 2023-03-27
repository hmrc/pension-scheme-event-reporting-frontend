package models.controllers.event13

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class SchemeStructureSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "SchemeStructure" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(SchemeStructure.values.toSeq)

      forAll(gen) {
        schemeStructure =>

          JsString(schemeStructure.toString).validate[SchemeStructure].asOpt.value mustEqual schemeStructure
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!SchemeStructure.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[SchemeStructure] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(SchemeStructure.values.toSeq)

      forAll(gen) {
        schemeStructure =>

          Json.toJson(schemeStructure) mustEqual JsString(schemeStructure.toString)
      }
    }
  }
}
