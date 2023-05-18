/*
 * Copyright 2023 HM Revenue & Customs
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

package fileUploadParser

import base.SpecBase
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

class CSVParserSpec extends SpecBase with Matchers with MockitoSugar with BeforeAndAfterEach {

  "CSV Parser must" - {
    "parse where the string is empty" in {
      CSVParser.split("") mustBe Seq()
    }

    "parse for a simple string of elements without quotes with multiple rows in the CSV with no quotes" in {
      val validCSVContent: String =
        s"""Joe,Bloggs,AB123456C
Jane,Blaggs,ZZ123456C"""

      val actual: Seq[Seq[String]] = CSVParser.split(validCSVContent).map(_ .toSeq)
      val expected: Seq[Seq[String]] = Seq(Seq("Joe", "Bloggs", "AB123456C"), Seq("Jane", "Blaggs", "ZZ123456C"))

      actual mustBe expected
    }

    "parse where there are double quotes in string i.e. remove the extra quotes" in {
      CSVParser.split(""""Joe","Bloggs","AB123456C"""").flatten mustBe Seq("Joe", "Bloggs", "AB123456C")
    }
  }
}
