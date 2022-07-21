package pages

import java.time.LocalDate

import controllers.routes
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object $className$Page extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "$className;format="decap"$"

  override def route(waypoints: Waypoints): Call =
    routes.$className$Controller.onPageLoad(waypoints)
}
