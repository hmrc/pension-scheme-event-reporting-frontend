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

package forms.behaviours

import play.api.data.{Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

trait StringFieldBehaviours extends FieldBehaviours {

  def fieldWithMaxLength(form: Form[?],
                         fieldName: String,
                         maxLength: Int,
                         lengthError: FormError): Unit = {

    s"not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThan(maxLength) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain only lengthError
      }
    }
  }

  def optionalField[T](
                        form: Form[T],
                        fieldName: String,
                        validData: Map[String, String],
                        accessor: T => Option[String]
                      ): Unit = {

    "trim spaces" in {
      val value = validData(fieldName)
      forAll(RegexpGen.from("""^\s+""" + value + """\s+$""")) { s =>
        val result = form.bind(validData.updated(fieldName, s))
        accessor(result.get) mustBe Some(value)
      }
    }

  }

  def fieldWithMinLength(form: Form[?],
                         fieldName: String,
                         minLength: Int,
                         lengthError: FormError): Unit = {

    s"not bind strings shorter than $minLength characters" in {

      forAll(stringsShorterThan(minLength) -> "shortString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain only lengthError
      }
    }
  }

  def fieldWithRegex(form: Form[?],
                     fieldName: String,
                     invalidString: String,
                     error: FormError): Unit = {

    s"not bind string $invalidString invalidated by regex " in {
      val result = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
      result.errors mustEqual Seq(error)
    }
  }

  def fieldWithTransform[A, B](form: Form[A],
                               transformName: String,
                               data: Map[String, String],
                               expected: B,
                               actual: A => B): Unit = {
    s"apply field transform $transformName" in {
      val result = form.bind(data)
      result.errors.size mustBe 0
      actual(result.get) mustBe expected
    }
  }
}
