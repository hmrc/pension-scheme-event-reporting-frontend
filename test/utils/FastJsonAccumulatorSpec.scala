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

package utils

import base.SpecBase
import play.api.libs.json.{IdxPathNode, JsNumber, JsPath, JsString, Json, KeyPathNode}
import services.fileUpload.CommitItem

class FastJsonAccumulatorSpec extends SpecBase {
   "must be able to accumulate correct json for bulk upload" in {
     val accumulator = new FastJsonAccumulator
     def addRow(number: Int) = {
       accumulator.addItem(
         CommitItem(
           JsPath(
             List(
               KeyPathNode("initialObject"),
               KeyPathNode("array"),
               IdxPathNode(number),
               KeyPathNode("obj")
             ),
           ),
           JsString("value" + number)
         ),
         number
       )
       accumulator.addItem(
         CommitItem(
           JsPath(
             List(
               KeyPathNode("initialObject"),
               KeyPathNode("array"),
               IdxPathNode(number),
               KeyPathNode("obj2")
             ),
           ),
           JsNumber(number)
         ),
         number
       )
     }

     (1 to 10).foreach(addRow)
     val expected = Json.parse(
       """{
  "initialObject": {
    "array": [
      {
        "obj2": 1,
        "obj": "value1"
      },
      {
        "obj2": 2,
        "obj": "value2"
      },
      {
        "obj2": 3,
        "obj": "value3"
      },
      {
        "obj2": 4,
        "obj": "value4"
      },
      {
        "obj2": 5,
        "obj": "value5"
      },
      {
        "obj2": 6,
        "obj": "value6"
      },
      {
        "obj2": 7,
        "obj": "value7"
      },
      {
        "obj2": 8,
        "obj": "value8"
      },
      {
        "obj2": 9,
        "obj": "value9"
      },
      {
        "obj2": 10,
        "obj": "value10"
      }
    ]
  }
}""")
     accumulator.toJson mustBe expected
   }

}
