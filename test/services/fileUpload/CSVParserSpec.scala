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

import base.SpecBase
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

import java.io.ByteArrayInputStream
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class CSVParserSpec extends SpecBase with Matchers with MockitoSugar with BeforeAndAfterEach {

  def parseResult(data: String): (ArrayBuffer[Array[String]], Int) = {
    Await.result(CSVParser.split(new ByteArrayInputStream(data.getBytes("UTF-8")))(new ArrayBuffer[Array[String]]()) {
      case (acc, row, rowNumber) => acc += row
    }, 5.seconds)
  }

  "CSV Parser must" - {
    "parse where the string is empty" in {
      val (result, rowNumber) = parseResult("")

      result mustBe Seq()
      rowNumber mustBe 0
    }

    "parse for a simple string of elements without quotes with multiple rows in the CSV with no quotes" in {
      val validCSVContent: String =
        s"""Joe,Bloggs,AB123456C
Jane,Blaggs,ZZ123456C"""

      val (actual, rowNumber) = parseResult(validCSVContent)

      val expected: Seq[Seq[String]] = Seq(Seq("Joe", "Bloggs", "AB123456C"), Seq("Jane", "Blaggs", "ZZ123456C"))

      actual.flatten.toSeq mustBe expected.flatten
      rowNumber mustBe 2
    }

    "parse where there are double quotes in string i.e. remove the extra quotes" in {
      val (actual, rowNumber) = parseResult(""""Joe","Bloggs","AB123456C"""")

      actual.flatten mustBe Seq("Joe", "Bloggs", "AB123456C")
    }

    "work where there are no double quotes in string" in {
      parseResult("a,b,c")._1.flatten mustBe Seq("a", "b", "c")
    }

    "work for double quotes containing comma at start of list" in {
      parseResult(""""a,b",c,d""")._1.flatten mustBe Seq(
        """a,b""",
        "c",
        "d"
      )
    }

    "work for double quotes containing comma in middle of list" in {
      parseResult("""a,"b,c",d""")._1.flatten mustBe Seq(
        "a",
        """b,c""",
        "d"
      )
    }

    "work for double quotes containing comma at end of list" in {
      parseResult("""a,b,"c,d"""")._1.flatten mustBe Seq(
        "a",
        "b",
        """c,d"""
      )
    }

    "work for double quotes containing no comma in middle of list" in {
      parseResult("""a,"b and c",d""")._1.flatten mustBe Seq(
        "a",
        """b and c""",
        "d"
      )
    }

    "leave double quotes containing no comma in middle of list when not first and last characters" in {
      parseResult("""a,b"c"d,e""")._1.flatten mustBe Seq(
        "a",
        """b"c"d""",
        "e"
      )
    }

    "leave double quotes containing a comma in middle of list when not first and last characters" in {
      parseResult("a,\"bc,xd\",e")._1.flatten mustBe Seq(
        "a",
        """bc,xd""",
        "e"
      )
    }

    "work for double quotes containing comma at end of list but quotes not ended" in {
      parseResult("""a,b,"c,d""")._1.flatten mustBe Seq(
        "a",
        "b",
        """c,d"""
      )
    }

    "work for double quotes containing comma at end of list but ending with a comma" in {
      parseResult("""a,b,"c,d",""")._1.flatten mustBe Seq(
        "a",
        "b",
        """c,d""",
        ""
      )
    }

    "strip trailing and leading spaces" in {
      parseResult(""" a , "b and c"  ,   d   """)._1.flatten mustBe Seq(
        "a",
        "b and c",
        "d"
      )
    }
  }
}
