package pages$if(package.empty)$$else$.$package$$endif$

import controllers$if(!package.empty)$.$package$$endif$.routes
import play.api.libs.json.JsPath
import play.api.mvc.Call
$if(!package.empty)$
import pages.{Waypoints, QuestionPage}
$endif$

case object $className$Page extends QuestionPage[Int] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"

  override def route(waypoints: Waypoints): Call =
    routes.$className$Controller.onPageLoad(waypoints)
}
