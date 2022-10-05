package models.event1

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class RefundDetailsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "RefundDetails" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(RefundDetails.values.toSeq)

      forAll(gen) {
        refundDetails =>

          JsString(refundDetails.toString).validate[RefundDetails].asOpt.value mustEqual refundDetails
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!RefundDetails.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[RefundDetails] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(RefundDetails.values.toSeq)

      forAll(gen) {
        refundDetails =>

          Json.toJson(refundDetails) mustEqual JsString(refundDetails.toString)
      }
    }
  }
}
