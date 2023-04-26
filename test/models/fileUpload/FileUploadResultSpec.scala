package models.fileUpload

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class FileUploadResultSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "FileUploadResult" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(FileUploadResult.values.toSeq)

      forAll(gen) {
        fileUploadResult =>

          JsString(fileUploadResult.toString).validate[FileUploadResult].asOpt.value mustEqual fileUploadResult
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!FileUploadResult.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[FileUploadResult] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(FileUploadResult.values.toSeq)

      forAll(gen) {
        fileUploadResult =>

          Json.toJson(fileUploadResult) mustEqual JsString(fileUploadResult.toString)
      }
    }
  }
}
