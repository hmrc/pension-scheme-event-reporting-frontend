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

package services.fileUpload

import com.univocity.parsers.common.ParsingContext
import com.univocity.parsers.common.processor.RowProcessor
import com.univocity.parsers.csv.{CsvParser, CsvParserSettings}

import java.io._
import scala.concurrent.{Future, Promise}
import scala.util.Success

private final class AsyncRowProcessor[Y](acc:Y)(rowExecutor: (Y, Array[String], Int) => Unit) extends RowProcessor {

  private val promise = Promise.apply[(Y, Int)]()
  val future: Future[(Y, Int)] = promise.future
  private var rowNumber = 0

  override def processStarted(context: ParsingContext): Unit = {}

  override def rowProcessed(row: Array[String], context: ParsingContext): Unit = {
    rowExecutor(acc, row.map(_.replaceAll("\\p{C}", "")), rowNumber)
    rowNumber = rowNumber + 1
  }

  override def processEnded(context: ParsingContext): Unit = promise.complete(Success(acc -> rowNumber))
}

object CSVParser {

  def split[Y](inputStream: InputStream)(acc:Y)(rowExecutor: (Y, Array[String], Int) => Unit): Future[(Y, Int)] = {
    val processor = new AsyncRowProcessor(acc)(rowExecutor)
    val settings = new CsvParserSettings()
    settings.setNullValue("")
    settings.setEmptyValue("")
    settings.setProcessor(processor)
    val parser = new CsvParser(settings)
    parser.parse(inputStream)
    processor.future
  }

}