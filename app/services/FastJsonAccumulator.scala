package services

import play.api.libs.json.{JsObject, JsValue, Json, Writes}
import services.fileUpload.CommitItem
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Try

class FastJsonAccumulator {

  private case class JsonStructure(var array: Option[ArrayBuffer[JsonStructure]] = None,
                           var obj: Option[mutable.Map[String, JsonStructure]] = None,
                           var value: Option[JsValue] = None)

  implicit lazy val jsonStructureWrites: Writes[JsonStructure] = new Writes[JsonStructure] {
    override def writes(o: JsonStructure): JsValue = {
      (o.array, o.obj, o.value) match {
        case (Some(value), _, _) => Json.toJson(value.toSeq)
        case (_, Some(value), _) => Json.toJson(value)
        case (_, _, Some(value)) => Json.toJson(value)
        case _ => throw new RuntimeException("Incorrect format for FastJsonAccumulator")
      }
    }
  }

  private val dataAccumulator = JsonStructure()
  def addItem(commitItem: CommitItem, rowNumber: Int): Unit = {
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
      } else if(pathString.startsWith(".") || pathString.startsWith("[")) {
        val objName = pathString.tail

        def createCurDataLocation(): Unit = {
          val newDataLocation = JsonStructure()
          curDataLocation.obj = Some(mutable.Map(objName -> newDataLocation))
          curDataLocation = newDataLocation
        }

        val opt = curDataLocation.obj
        if(opt.isEmpty) {
          opt.getOrElse(createCurDataLocation())
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

  def toJson: JsValue = Json.toJson(dataAccumulator)
}
