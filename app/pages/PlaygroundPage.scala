package pages

import java.time.LocalDate

import controllers.routes
import play.api.libs.json.JsPath
import play.api.mvc.Call
case object PlaygroundPage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "playground"

  override def route(waypoints: Waypoints): Call =
    routes.PlaygroundController.onPageLoad(waypoints)
}
