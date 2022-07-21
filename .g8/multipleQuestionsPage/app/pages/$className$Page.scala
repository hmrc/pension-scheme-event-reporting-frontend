package pages

import controllers.routes
import models.$className$
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object $className$Page extends QuestionPage[$className$] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"

  override def route(waypoints: Waypoints): Call =
    routes.$className$Controller.onPageLoad(waypoints)
}
