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

import play.api.Logging
import play.api.libs.json.{JsValue, Json, Writes}
import services.fileUpload.CommitItem

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Try


protected case class JsonStructure(var array: Option[ArrayBuffer[JsonStructure]] = None,
                         var obj: Option[mutable.Map[String, JsonStructure]] = None,
                         var value: Option[JsValue] = None)

protected object JsonStructure {
  implicit lazy val jsonStructureWrites: Writes[JsonStructure] = new Writes[JsonStructure] {
    override def writes(o: JsonStructure): JsValue = {
      (o.array, o.obj, o.value) match {
        case (Some(value), None, None) => Json.toJson(value.toSeq)
        case (None, Some(value), None) => Json.toJson(value)
        case (None, None, Some(value)) => Json.toJson(value)
        case _ => throw new RuntimeException()
      }
    }
  }
}


class FastJsonAccumulator extends Logging {

  private val dataAccumulator = JsonStructure()
  def addItem(commitItem: CommitItem, rowNumber: Int): Unit = {
    logger.debug(
      s"""
        |Commit item:
        |$commitItem
        |Row: $rowNumber
        |""".stripMargin)
    var curDataLocation: JsonStructure = dataAccumulator
    val path = commitItem.jsPath.path
    val lastPathIndex = commitItem.jsPath.path.size - 1
    path.zipWithIndex.foreach { case (path, location) =>
      val pathString = path.toJsonString
      if(pathString.startsWith("[")) {
        val array = curDataLocation.array
        if(array.isEmpty) {
          val newDataLocation = JsonStructure()
          curDataLocation.array = Some(ArrayBuffer(JsonStructure()))
          curDataLocation = newDataLocation
        } else if(Try(curDataLocation.array.get(rowNumber - 2)).toOption.isDefined) {
          curDataLocation = curDataLocation.array.get.apply(rowNumber - 2)
        } else {
          val newDataLocation = JsonStructure()
          curDataLocation.array.get.addOne(newDataLocation)
          curDataLocation = newDataLocation
        }
      } else if(pathString.startsWith(".")) {
        val objName = pathString.tail

        def createCurDataLocation(): Unit = {
          val newDataLocation = JsonStructure()
          curDataLocation.obj = Some(mutable.Map(objName -> newDataLocation))
          curDataLocation = newDataLocation
        }

        val opt = curDataLocation.obj
        if(opt.isEmpty) {
          if(opt.isEmpty) {
            createCurDataLocation()
          }
        } else {
          val map = curDataLocation.obj.get
          if(!map.contains(objName)) {
            val newDataLocation = JsonStructure()
            map += (objName -> newDataLocation)
            curDataLocation = newDataLocation
          } else {
            curDataLocation = map(objName)
          }
        }
      }
      if(location == lastPathIndex) {
        curDataLocation.value = Some(commitItem.value)
      }
    }
  }

  def toJson: JsValue = try {
    Json.toJson(dataAccumulator)
  } catch {
    case _:RuntimeException => throw new RuntimeException(
      s"""Incorrect format for FastJsonAccumulator
         |Structure:
         |$dataAccumulator
         |""".stripMargin)
  }
}
