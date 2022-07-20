package pages

import controllers.routes
import models.UserAnswers
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object $className$Page extends QuestionPage[Boolean] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"

  override def route(waypoints: Waypoints): Call =
    routes.$className$Controller.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.get(this).map {
      case true  => this
      case false => this
    }.orRecover
  }
}
