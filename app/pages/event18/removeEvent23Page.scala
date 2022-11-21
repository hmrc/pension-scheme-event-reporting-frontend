package pages.event18

import controllers.event18.routes
import models.UserAnswers
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.{Page, QuestionPage, Waypoints}

case object removeEvent23Page extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "removeEvent23"

  override def route(waypoints: Waypoints): Call =
    routes.removeEvent23Controller.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this).map {
      case true  => this
      case false => this
    }.orRecover
  }
}
