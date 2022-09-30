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

package forms.mappings

import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpec

class TransformsSpec extends AnyWordSpec with Matchers with TableDrivenPropertyChecks with Transforms {

  "noTransform" must {
    "leave its input unchanged" in {
      val expected = " a B c "
      val actual = noTransform(expected)
      actual mustBe expected
    }
  }

  "toUpperCaseAlphaOnly" must {
    "convert Alpha to uppercase" in {
      val actual = toUpperCaseAlphaOnly("abcdef")
      actual mustBe "ABCDEF"
    }
    "convert Alpha to uppercase not control character" in {
      val actual = toUpperCaseAlphaOnly("""abcÿdef""")
      actual mustBe """ABCÿDEF"""
    }
  }

}
