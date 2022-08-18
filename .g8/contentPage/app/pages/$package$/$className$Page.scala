package pages$if(!package.empty)$.$package$$endif$

import controllers.routes
import play.api.mvc.Call
$if(!package.empty)$
import pages.{Waypoints, Page}
$endif$

case object $className$Page extends Page {

  override def route(waypoints: Waypoints): Call =
    routes.$className$Controller.onPageLoad(waypoints)
}
