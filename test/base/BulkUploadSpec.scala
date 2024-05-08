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

package base

import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.i18n.Messages
import services.fileUpload.{CSVParser, Event1Validator, ValidationError}
import utils.FastJsonAccumulator

import java.io.ByteArrayInputStream
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class BulkUploadSpec(headerConfig: String) extends SpecBase with Matchers with MockitoSugar {
  private val injector = applicationBuilder(None).injector()
  val header: String = injector.instanceOf[Configuration].get[String](headerConfig)
  private val validator = injector.instanceOf[Event1Validator]

  def validate(data: String)(implicit messages: Messages): ((FastJsonAccumulator, ArrayBuffer[ValidationError]), Int) = {
    val inputStream = new ByteArrayInputStream(data.getBytes("UTF-8"))
    val result = CSVParser.split(inputStream)(new FastJsonAccumulator() -> new ArrayBuffer[ValidationError]()) { case ((dataAccumulator, errorAccumulator), row, rowNumber) =>
      validator.validate(rowNumber, row, dataAccumulator, errorAccumulator, 2022)
    }
    Await.result(result, 5.seconds)
  }
}
