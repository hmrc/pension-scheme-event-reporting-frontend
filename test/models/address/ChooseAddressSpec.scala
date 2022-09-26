package models.address

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class ChooseAddressSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ChooseAddress" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ChooseAddress.values.toSeq)

      forAll(gen) {
        chooseAddress =>

          JsString(chooseAddress.toString).validate[ChooseAddress].asOpt.value mustEqual chooseAddress
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ChooseAddress.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ChooseAddress] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ChooseAddress.values.toSeq)

      forAll(gen) {
        chooseAddress =>

          Json.toJson(chooseAddress) mustEqual JsString(chooseAddress.toString)
      }
    }
  }
}
