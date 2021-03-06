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

package generators

import models.{TestCheckBox, TestRadioButton}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages.{TestCheckBoxPage, TestDatePage, TestRadioButtonPage}
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryTestRadioButtonUserAnswersEntry: Arbitrary[(TestRadioButtonPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TestRadioButtonPage.type]
        value <- arbitrary[TestRadioButton].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTestCheckBoxUserAnswersEntry: Arbitrary[(TestCheckBoxPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TestCheckBoxPage.type]
        value <- arbitrary[TestCheckBox].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWibbleUserAnswersEntry: Arbitrary[(TestDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TestDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }
}
