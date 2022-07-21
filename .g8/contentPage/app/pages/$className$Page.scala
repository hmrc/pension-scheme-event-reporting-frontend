package pages

import controllers.routes
import play.api.mvc.Call

case object $className$Page extends Page {

  override def route(waypoints: Waypoints): Call =
    routes.$className$Controller.onPageLoad(waypoints)
}
